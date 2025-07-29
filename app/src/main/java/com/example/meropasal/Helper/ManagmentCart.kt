package com.example.project1762.Helper

import android.content.Context
import android.widget.Toast
import com.example.meropasal.Model.ItemsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagmentCart(val context: Context) {

    private val database = FirebaseDatabase.getInstance()
    private fun getCurrentUserId(): String? {
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPref.getString("current_user_id", null)
    }

    fun insertItem(item: ItemsModel) {
        val userId = getCurrentUserId() ?: return
        getListCart { listItem ->
            val existAlready = listItem.any { it.title == item.title }
            val index = listItem.indexOfFirst { it.title == item.title }

            if (existAlready) {
                listItem[index].numberInCart = item.numberInCart
            } else {
                listItem.add(item)
            }
            
            database.getReference("carts").child(userId).setValue(listItem)
            Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
        }
    }

    fun getListCart(callback: (ArrayList<ItemsModel>) -> Unit) {
        val userId = getCurrentUserId()
        if (userId == null) {
            callback(arrayListOf())
            return
        }
        
        database.getReference("carts").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cartItems = arrayListOf<ItemsModel>()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(ItemsModel::class.java)
                        item?.let { cartItems.add(it) }
                    }
                    callback(cartItems)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(arrayListOf())
                }
            })
    }

    fun getListCart(): ArrayList<ItemsModel> {
        // This method is kept for backward compatibility but should be avoided
        // Use the callback version instead
        return arrayListOf()
    }

    fun minusItem(listItem: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        val userId = getCurrentUserId() ?: return
        
        if (listItem[position].numberInCart == 1) {
            listItem.removeAt(position)
        } else {
            listItem[position].numberInCart--
        }
        
        database.getReference("carts").child(userId).setValue(listItem)
        listener.onChanged()
    }

    fun plusItem(listItem: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        val userId = getCurrentUserId() ?: return
        
        listItem[position].numberInCart++
        database.getReference("carts").child(userId).setValue(listItem)
        listener.onChanged()
    }

    fun getTotalFee(callback: (Double) -> Unit) {
        getListCart { listItem ->
            var fee = 0.0
            for (item in listItem) {
                fee += item.price * item.numberInCart
            }
            callback(fee)
        }
    }

    fun clearCart() {
        val userId = getCurrentUserId() ?: return
        database.getReference("carts").child(userId).removeValue()
    }
}