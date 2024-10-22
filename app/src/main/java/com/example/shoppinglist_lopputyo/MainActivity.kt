package com.example.shoppinglist_lopputyo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class MainActivity : ComponentActivity() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val shoppingListRef = firestore.collection("shoppingList")
    private var shoppingItems by mutableStateOf<List<ShoppingItem>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            ShoppingListApp()
        }

        readShoppingList()
    }

    @Composable
    fun ShoppingListApp() {

        var shoppingItem by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            OutlinedTextField(
                value = shoppingItem,
                onValueChange = { shoppingItem = it },
                label = { Text("Ostos") }
            )


            Button(
                onClick = {
                    if (shoppingItem.isNotEmpty()) {

                        val newItemRef = shoppingListRef.document() // Luo uusi dokumentti
                        newItemRef.set(hashMapOf("item" to shoppingItem))
                            .addOnSuccessListener {
                                Log.d("Firestore", "Ostos tallennettu: $shoppingItem")

                                readShoppingList()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Virhe tallennettaessa ostosta", e)
                            }

                        shoppingItem = ""
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Tallenna ostos")
            }

            // Näytetään ostoslista
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(shoppingItems) { item ->
                    ShoppingListItem(item) {
                        // Poista ostos Firestoresta
                        removeShoppingItem(item)
                    }
                }
            }
        }
    }

    @Composable
    fun ShoppingListItem(item: ShoppingItem, onDelete: () -> Unit) {
        // Näytetään ostoksen nimi ja poisto-nappi
        Row(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
            Text(item.name, modifier = Modifier.weight(1f))
            Button(onClick = onDelete) {
                Text("Poista")
            }
        }
    }


    private fun readShoppingList() {
        shoppingListRef.get()
            .addOnSuccessListener { documents ->
                shoppingItems = documents.map { document ->
                    ShoppingItem(document.id, document.getString("item") ?: "")
                }
                Log.d("Firestore", "Ostokset luettu: $shoppingItems")
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Virhe tietojen hakemisessa", exception)
            }
    }


    private fun removeShoppingItem(item: ShoppingItem) {
        shoppingListRef.document(item.id).delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Ostos poistettu: ${item.name}")
                // Lue ostoslista uudelleen poistamisen jälkeen
                readShoppingList()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Virhe poistettaessa ostosta", e)
            }
    }
}


data class ShoppingItem(val id: String, val name: String)