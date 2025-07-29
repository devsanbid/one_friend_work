package com.example.meropasal.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.meropasal.Activity.OrderActivity
import com.example.meropasal.Model.ItemsModel
import com.example.project1762.Helper.ManagmentCart
import com.example.meropasal.R
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import coil.compose.rememberAsyncImagePainter
import com.example.project1762.Helper.ChangeNumberItemsListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class CartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            CartScreen ( ManagmentCart(this),
            onBackClick={
                finish()
            })
        }
    }
}

fun calculatorCart(managmentCart: ManagmentCart, tax: MutableState<Double>){
    val percentTax=0.02
    managmentCart.getTotalFee { totalFee ->
        tax.value=Math.round((totalFee * percentTax)*100)/100.0
    }
}

@Composable
private fun CartScreen(
    managmentCart: ManagmentCart= ManagmentCart(LocalContext.current),
    onBackClick: ()-> Unit
){
    val cartItems= remember { mutableStateOf(arrayListOf<ItemsModel>()) }
    val tax = remember { mutableStateOf(0.0) }
    val itemTotal = remember { mutableStateOf(0.0) }
    
    LaunchedEffect(Unit) {
        managmentCart.getListCart { items ->
            cartItems.value = items
        }
        managmentCart.getTotalFee { total ->
            itemTotal.value = total
        }
        calculatorCart(managmentCart, tax)
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),

    ) {
        ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
            val(backBtn, cartTxt)= createRefs()
            Text(modifier = Modifier
                .fillMaxWidth()
                .constrainAs(cartTxt) { centerTo(parent) },
                text = "Your Cart",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
                )
            Image(painter = painterResource(R.drawable.back),
                contentDescription = null,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
                )
        }
        if(cartItems.value.isEmpty()){
            Text(text = "Cart Is Empty", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            CartList(cartItems=cartItems.value, managmentCart){
                managmentCart.getListCart { items ->
                    cartItems.value = items
                }
                managmentCart.getTotalFee { total ->
                    itemTotal.value = total
                }
                calculatorCart(managmentCart, tax)
            }
            CartSummary(
                itemTotal=itemTotal.value,
                tax = tax.value,
                delivery = 10.0,
                cartItems = cartItems.value,
                managementCart = managmentCart
            )
        }
    }
}

@Composable
fun CartSummary(
    itemTotal: Double,
    tax: Double,
    delivery: Double,
    cartItems: ArrayList<ItemsModel>,
    managementCart: ManagmentCart
) {
    val context = LocalContext.current
    val total = itemTotal + tax + delivery
    var isCheckingOut by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        ){
            Text(
                text = "Item Total:",
                Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkBrown)
            )
            Text(
                text = "$$itemTotal"
            )
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        ){
            Text(
                text = "Tax:",
                Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkBrown)
            )
            Text(
                text = "$$tax"
            )
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        ){
            Text(
                text = "Delivery:",
                Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkBrown)
            )
            Text(
                text = "$$delivery"
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        ){
            Text(
                text = "Total:",
                Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkBrown)
            )
            Text(
                text = "$$total"
            )
        }
        Button(
            onClick = {
                if (cartItems.isNotEmpty()) {
                    isCheckingOut = true
                    processCheckout(
                        cartItems = cartItems,
                        totalAmount = total,
                        context = context,
                        managementCart = managementCart,
                        onSuccess = {
                            isCheckingOut = false
                            Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                            context.startActivity(Intent(context, OrderActivity::class.java))
                            (context as CartActivity).finish()
                        },
                        onError = { error ->
                            isCheckingOut = false
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isCheckingOut && cartItems.isNotEmpty(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.darkBrown)
            ),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isCheckingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Check Out",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}

fun processCheckout(
    cartItems: ArrayList<ItemsModel>,
    totalAmount: Double,
    context: android.content.Context,
    managementCart: ManagmentCart,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onError("Please login to place order")
        return
    }
    
    val database = FirebaseDatabase.getInstance()
    val orderId = UUID.randomUUID().toString()
    
    val orderData = hashMapOf(
        "orderId" to orderId,
        "items" to cartItems.map { item ->
            hashMapOf(
                "title" to item.title,
                "description" to item.description,
                "price" to item.price,
                "picUrl" to item.picUrl,
                "numberInCart" to item.numberInCart
            )
        },
        "totalAmount" to totalAmount,
        "orderDate" to System.currentTimeMillis(),
        "status" to "Processing",
        "deliveryAddress" to "Default Address"
    )
    
    database.reference.child("Orders").child(currentUser.uid).child(orderId)
        .setValue(orderData)
        .addOnSuccessListener {
            managementCart.clearCart()
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onError(exception.message ?: "Failed to place order")
        }
}

@Composable
fun CartList(cartItems: ArrayList<ItemsModel>, managementCart: ManagmentCart, onItemChange:() -> Unit) {
    LazyColumn(Modifier.padding(top=16.dp)) {
        items(cartItems) {item->
            CartItem(
                cartItems,
                item=item,
                managementCart= managementCart,
                onItemChange= onItemChange
            )
        }
    }
}

@Composable
fun CartItem(
    cartItems: ArrayList<ItemsModel>,
    item: ItemsModel,
    managementCart: ManagmentCart,
    onItemChange: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        val(pic,titleTxt, feeEachTime,totalEachItem, Quantity) = createRefs()

        Image(
            painter = rememberAsyncImagePainter(item.picUrl[0]),
            contentDescription = null,
            modifier = Modifier
                .size(90.dp)
                .background(
                    colorResource(R.color.lightBrown),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(8.dp)
                .constrainAs(pic) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )
        Text(text = item.title,
            modifier = Modifier
                .constrainAs(titleTxt) {
                    start.linkTo(pic.end)
                    top.linkTo(pic.top)
                }
                .padding(start = 8.dp, top = 8.dp)
            )
        Text(text = "$${item.price}", color = colorResource(R.color.darkBrown),
            modifier = Modifier
                .constrainAs(feeEachTime) {
                    start.linkTo(titleTxt.start)
                    top.linkTo(titleTxt.bottom)
                }
                .padding(start = 8.dp, top = 8.dp)
        )
        Text(text = "$${item.numberInCart*item.price}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .constrainAs(totalEachItem) {
                    start.linkTo(titleTxt.start)
                    bottom.linkTo(pic.bottom)
                }
                .padding(start = 8.dp)
        )
        ConstraintLayout(
            modifier = Modifier
                .width(100.dp)
                .constrainAs(Quantity) {
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .background(
                    colorResource(R.color.lightBrown),
                    shape = RoundedCornerShape(100.dp)
                )
        ) {
            val (plusCartBtn, minusCartBtn, numberItemText) = createRefs()
            Text(text = item.numberInCart.toString(), color = Color.Black, fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(numberItemText){
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            )

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(28.dp)
                    .background(
                        colorResource(R.color.darkBrown),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .constrainAs(plusCartBtn) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable {
                        managementCart.plusItem(
                            cartItems,
                            cartItems.indexOf(item),
                            object : ChangeNumberItemsListener {
                                override fun onChanged() {
                                    onItemChange()
                                }

                            }
                        )
                    }
            ){
                Text(
                    text = "+",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(28.dp)
                    .background(
                        colorResource(R.color.white),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .constrainAs(minusCartBtn) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable {
                        managementCart.minusItem(
                            cartItems,
                            cartItems.indexOf(item), object : ChangeNumberItemsListener {
                                override fun onChanged() {
                                    onItemChange()
                                }

                            })
                    }
            ){
                Text(
                    text = "-",
                    color = colorResource(R.color.darkBrown),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

