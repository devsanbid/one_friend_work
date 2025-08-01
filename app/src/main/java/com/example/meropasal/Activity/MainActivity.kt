package com.example.meropasal.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.meropasal.Model.ItemsModel
import com.example.meropasal.Model.SliderModel
import com.example.meropasal.R
import com.example.meropasal.ViewModel.MainViewModel
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState

class MainActivity : BaseActivity() {
    private var shouldRefresh = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainActivityScreen(
                refreshTrigger = shouldRefresh,
                onCartClick = {
                    startActivity(Intent(this, CartActivity::class.java))
                },
                onAddProductClick = {
                    startActivity(Intent(this, AddProductActivity::class.java))
                },
                onOrdersClick = {
                    startActivity(Intent(this, OrderActivity::class.java))
                },
                onProfileClick = {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        shouldRefresh = !shouldRefresh // Toggle to trigger recomposition
    }
}

@Composable
fun MainActivityScreen(
    refreshTrigger: Boolean,
    onCartClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit
){
    val viewModel= MainViewModel()
    val context = LocalContext.current
    var username by remember { mutableStateOf("User") }
    var refreshKey by remember { mutableStateOf(0) }
    
    fun refreshData() {
        refreshKey++
    }
    
    LaunchedEffect(refreshTrigger, refreshKey) {
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getString("current_user_id", null)
        
        currentUserId?.let { userId ->
            val database = FirebaseDatabase.getInstance()
            database.reference.child("Users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fullName = snapshot.child("fullName").getValue(String::class.java)
                        username = fullName ?: "User"
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    val banners = remember { mutableStateListOf<SliderModel>() }
    val Popular= remember { mutableStateListOf<ItemsModel>() }

    var showBannerLoading by remember { mutableStateOf(true) }
    var showPopularLoading by remember { mutableStateOf(true) }


    // banner
    LaunchedEffect(refreshTrigger, refreshKey) {
        showBannerLoading = true
        viewModel.loadBanner().observeForever{
            banners.clear()
            banners.addAll(it)
            showBannerLoading=false
        }
    }



    // Popular
    LaunchedEffect(refreshTrigger, refreshKey) {
        showPopularLoading = true
        viewModel.loadPopular().observeForever {
            Popular.clear()
            Popular.addAll(it)
            showPopularLoading=false
        }

    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){
            item {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column {
                        Text("Welcome Back", color = Color.Black)
                        Text(username,
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                            )
                    }
                    Row {
                        Image(
                            painter = painterResource(R.drawable.search_icon),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = { refreshData() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(R.drawable.bell_icon),
                            contentDescription = null
                        )
                    }
                }
            }

            // bannners
            item {
                if(showBannerLoading){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(200.dp),
                        contentAlignment = Alignment.Center

                    ){
                        CircularProgressIndicator()
                    }
                } else {
                    Banners(banners)
                }
            }

            item {
                SectionTitle("All Products", "")
            }
            item {
                if(showPopularLoading){
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                        contentAlignment = Alignment.Center
                    ){
                        CircularProgressIndicator()
                    }
                } else {
                    ListItemsFullSize(Popular)
                }
            }
        }

        BottomMenu(
            modifier = Modifier
                .fillMaxWidth(),
            onCartClick = onCartClick,
            onAddProductClick = onAddProductClick,
            onOrdersClick = onOrdersClick,
            onProfileClick = onProfileClick
        )
    }
}




@Composable
fun SectionTitle(title: String, actionText: String) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top=24.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = actionText,
            color = colorResource(R.color.darkBrown)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Banners(banners: List<SliderModel>) {
    AutoSlidingCarousel(banners = banners)
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AutoSlidingCarousel(
    modifier: Modifier= Modifier.padding(top=16.dp),
    pagerState: PagerState= remember { PagerState() },
    banners: List<SliderModel>)
{
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    Column (modifier=modifier.fillMaxSize()) {
        HorizontalPager(count = banners.size, state = pagerState) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(banners[page].url)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
                    .height(150.dp)
            )

        }
        DotIndicator(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterHorizontally),
            totalDots = banners.size,
            selectedIndex = if(isDragged)pagerState.currentPage else pagerState.currentPage,
            dotSize = 8.dp
        )

    }
}

@Composable
fun DotIndicator(
    modifier: Modifier= Modifier,
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color= colorResource(R.color.darkBrown),
    unSelectedColor: Color= colorResource(R.color.grey),
    dotSize: Dp
){
    LazyRow(
        modifier = modifier
            .wrapContentSize()
    ) {
         items(totalDots){index->
             IndicatorDot(
                 color = if(index==selectedIndex)selectedColor else unSelectedColor,
                 size = dotSize
             )
             if(index!=  totalDots-1){
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
             }
         }
    }
}

@Composable
fun IndicatorDot(
    modifier: Modifier= Modifier,
    size: Dp,
    color: Color
){
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}


@Composable
fun BottomMenu(
    modifier: Modifier,
    onCartClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit
){
    Row (
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
            .background(
                colorResource(R.color.darkBrown),
                shape = RoundedCornerShape(10.dp)
            ),
        horizontalArrangement = Arrangement.SpaceAround
    ){
        BottomMenuItem(icon = painterResource(R.drawable.btn_1), text = "Explorer")
        BottomMenuItem(icon = painterResource(R.drawable.btn_2), text = "Cart", onItemClick = onCartClick)
        BottomMenuItem(icon = painterResource(R.drawable.btn_3), text = "Add Product", onItemClick = onAddProductClick)
        BottomMenuItem(icon = painterResource(R.drawable.btn_4), text = "Orders", onItemClick = onOrdersClick)
        BottomMenuItem(icon = painterResource(R.drawable.btn_5), text = "Profile", onItemClick = onProfileClick)
    }
}

@Composable
fun BottomMenuItem(icon: Painter, text: String, onItemClick: (() -> Unit)?=null){
    Column (modifier = Modifier
        .height(70.dp)
        .clickable{ onItemClick?.invoke()}
        .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Icon(icon, contentDescription = text, tint = Color.White)
        Spacer(modifier = Modifier.padding(vertical = 4.dp))
        Text(text, color = Color.White, fontSize = 10.sp)
    }
}