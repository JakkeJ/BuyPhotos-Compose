package com.example.buyphotos.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.DismissDirection
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import androidx.navigation.NavController
import androidx.tv.material3.ClickableSurfaceDefaults.color
import coil.compose.rememberAsyncImagePainter
import com.example.buyphotos.R
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.model.ArtViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ShoppingCartScreen(viewModel: ArtViewModel, navController: NavController) {
    LaunchedEffect(viewModel.updateCount) {
        viewModel.updateShoppingCartData()
    }
    val title = stringResource(R.string.shoppingCartTitle)
    LaunchedEffect(Unit){
        viewModel.setTitle(title)
    }
    val totalAmount = viewModel.totalNumberOfPhotos.collectAsState().value
    val totalSum = viewModel.basketTotalPrice.collectAsState().value
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ShoppingCartList(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Text(
                text = "Antall valgte bilder: $totalAmount"
            )
            Text(
                text = "Totalpris: $totalSum,-"
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceEvenly

        ) {
            Button(
                onClick = {
                    viewModel.emptyShoppingCart()
                    viewModel.updateCount++
                          },
            ) {
                Text(text = "TØM HANDLEKURV")
            }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val orderHeader = stringResource(id = R.string.orderHeader)
            val orderInfo = stringResource(id = R.string.orderInfo)
            val orderSubject = stringResource(id = R.string.orderSubject)
            val orderEnd = stringResource(id = R.string.orderEnd)

            Button(
                onClick = {
                    scope.launch {
                        println("Antall bilder totalt: ${ viewModel.totalNumberOfPhotos.value }")
                        submitOrder(viewModel,
                            context = context,
                            orderHeader = orderHeader,
                            orderInfo = orderInfo,
                            orderSubject = orderSubject,
                            orderEnd = orderEnd,
                            orderPrice = (viewModel.basketTotalPrice.value).toString(),
                            orderSize = viewModel.totalNumberOfPhotos.value.toString()
                        )
                    }
                    viewModel.emptyShoppingCart()
                },
            ) {
                Text(text = "SEND BESTILLING")
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartList(navController: NavController, viewModel: ArtViewModel, modifier: Modifier) {
    val artPhotos = viewModel.dbShoppingCart.collectAsState(initial = emptyList())
    LaunchedEffect(viewModel.updateCount) {
        viewModel.updateShoppingCartData()
    }
    LazyColumn(
        contentPadding = PaddingValues(
            8.dp
        )
    ) {
        items(artPhotos.value, key = { item: ShoppingCart -> item.id }) { artPhoto ->
            val dismissState = rememberDismissState()
            if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
                viewModel.removePhotoFromDb(artPhoto)
            }
            SwipeToDismiss(
                state = dismissState,
                directions = setOf(
                    DismissDirection.StartToEnd
                ),
                background = {
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> MaterialTheme.colorScheme.background
                            else -> Color(100,0,0)
                        }
                    )
                    val alignment = Alignment.CenterStart
                    val icon = Icons.Default.Delete

                    val scale by animateFloatAsState(
                        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color, shape = MaterialTheme.shapes.small)
                            .padding(horizontal = Dp(20f)),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            icon,
                            contentDescription = "Delete Icon",
                            modifier = Modifier
                                .scale(scale),
                            tint = Color.White
                        )
                    }
                },
                dismissContent = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(alignment = Alignment.CenterVertically),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        shape = MaterialTheme.shapes.small,
                        elevation = CardDefaults.cardElevation()
                    ) {
                        ShoppingCartPhotoCard(artPhoto = artPhoto, viewModel = viewModel)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ShoppingCartPhotoCard(artPhoto: ShoppingCart, viewModel: ArtViewModel) {
    var subtotal by remember { mutableStateOf(artPhoto.amount * artPhoto.price) }
    var amount by remember { mutableStateOf(artPhoto.amount) }

    val constraints = ConstraintSet {
        val photoComposable = createRefFor("photo_composable")
        val titleComposable = createRefFor("title_composable")
        val frameTypeComposable = createRefFor("frame_type_composable")
        val imageSizeComposable = createRefFor("image_size_composable")
        val removeImageButtonComposable = createRefFor("remove_image_button_composable")
        val pricePerPhotoComposable = createRefFor("price_per_photo_composable")
        val subTotalPriceComposable = createRefFor("sub_total_price_composable")
        val addImageButtonComposable = createRefFor("add_image_button_composable")
        val numberOfImagesComposable = createRefFor("number_of_images_composable")
        constrain(photoComposable) {
            height = Dimension.fillToConstraints
            top.linkTo(parent.top, 4.dp)
            start.linkTo(parent.start, 8.dp)
            bottom.linkTo(frameTypeComposable.top, 4.dp)
        }
        constrain(titleComposable) {
            top.linkTo(photoComposable.top)
            bottom.linkTo(photoComposable.bottom)
            start.linkTo(photoComposable.end, 4.dp)
            end.linkTo(removeImageButtonComposable.start, 4.dp)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        }
        constrain(frameTypeComposable) {
            bottom.linkTo(imageSizeComposable.top, 0.dp)
            start.linkTo(parent.start, 8.dp)
        }
        constrain(imageSizeComposable) {
            start.linkTo(parent.start, 8.dp)
            bottom.linkTo(parent.bottom, 0.dp)
        }
        constrain(removeImageButtonComposable) {
            end.linkTo(numberOfImagesComposable.start, 8.dp)
            bottom.linkTo(subTotalPriceComposable.top, 0.dp)
            top.linkTo(pricePerPhotoComposable.bottom, 0.dp)
        }
        constrain(numberOfImagesComposable) {
            end.linkTo(addImageButtonComposable.start, 8.dp)
            bottom.linkTo(subTotalPriceComposable.top, 0.dp)
            top.linkTo(pricePerPhotoComposable.bottom, 0.dp)
        }
        constrain(addImageButtonComposable) {
            end.linkTo(parent.end, 8.dp)
            bottom.linkTo(subTotalPriceComposable.top, 0.dp)
            top.linkTo(pricePerPhotoComposable.bottom, 0.dp)
        }
        constrain(pricePerPhotoComposable) {
            end.linkTo(parent.end, 8.dp)
            top.linkTo(parent.top, 0.dp)
        }
        constrain(subTotalPriceComposable) {
            end.linkTo(parent.end, 8.dp)
            bottom.linkTo(parent.bottom, 0.dp)
        }
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .height(120.dp)
    ) {
        ConstraintLayout(
            constraints,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = rememberAsyncImagePainter(artPhoto.imageUrl),
                contentDescription = "Art Photo",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .layoutId("photo_composable")
                    .aspectRatio(1f)
                    .clip(shape = MaterialTheme.shapes.small)
                    .border(
                        4.dp,
                        viewModel.getBorderColor(artPhoto.frameType),
                        shape = MaterialTheme.shapes.small
                    )
            )
            Column(modifier = Modifier
                .layoutId("title_composable"),
                horizontalAlignment = Alignment.Start,
                verticalArrangement =  Arrangement.Top
            ){
                Text(
                    text = artPhoto.imageTitle,
                    lineHeight = 16.sp,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "Rammetype: ${artPhoto.frameType}",
                modifier = Modifier
                    .layoutId("frame_type_composable"),
                fontSize = 11.sp
            )
            Text(
                text = "Bildestørrelse: ${artPhoto.imageSize}",
                modifier = Modifier
                    .layoutId("image_size_composable"),
                fontSize = 11.sp
            )
            Button(
                onClick = {
                    viewModel.decreasePhotoAmount(artPhoto)
                    subtotal = artPhoto.amount * artPhoto.price
                    amount = artPhoto.amount
                          },
                contentPadding = PaddingValues(
                    0.dp
                ),
                modifier = Modifier
                    .layoutId("remove_image_button_composable")
                    .height(50.dp)
                    .width(50.dp)
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        ""
                    )
                }
            Text(
                text = amount.toString(),
                modifier = Modifier
                    .layoutId("number_of_images_composable")
                    .width(20.dp),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {
                    viewModel.increasePhotoAmount(artPhoto)
                    subtotal = artPhoto.amount * artPhoto.price
                    amount = artPhoto.amount
                          },
                contentPadding = PaddingValues(
                    0.dp
                ),
                modifier = Modifier
                    .layoutId("add_image_button_composable")
                    .height(50.dp)
                    .width(50.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    ""
                )
            }
            Text(
                text = "Sum per bilde: ${artPhoto.price},-",
                modifier = Modifier
                    .layoutId("price_per_photo_composable"),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Subtotal: ${subtotal},-",
                modifier = Modifier
                    .layoutId("sub_total_price_composable"),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun submitOrder(viewModel: ArtViewModel,
                context: Context,
                orderSubject: String,
                orderHeader: String,
                orderInfo: String,
                orderEnd: String,
                orderSize: String,
                orderPrice: String
) {
    CoroutineScope(Dispatchers.IO).launch {
        val artPhotos = viewModel.getShoppingCartItems()
        var orderText = ""
        val emails = arrayOf("artphotoagent@some.domain")
        if (orderSize.toInt() != 0) {
            orderText += orderHeader
            for (items in artPhotos) {
                val orderDetails = orderInfo.format(
                    items.imageTitle, items.imageId,
                    items.imageSize, items.frameType, items.price, items.amount, (items.amount * items.price)
                )
                orderText += orderDetails
            }
            val orderEnding = orderEnd.format(
                orderSize,
                orderPrice
            )

            orderText += orderEnding

            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_EMAIL, emails)
                .putExtra(Intent.EXTRA_SUBJECT, orderSubject)
                .putExtra(Intent.EXTRA_TEXT, orderText)

            context.startActivity(intent)
        }
    }
}
