package com.example.buyphotos.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.buyphotos.MainActivity
import com.example.buyphotos.R
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.model.ArtPhotoApiStatus
import com.example.buyphotos.model.ArtViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.launch

@Composable
fun ShoppingCartScreen(viewModel: ArtViewModel, navController: NavController) {
    Column {
        Text(
            text = "Handlekurv",
            modifier = Modifier
                .padding(8.dp)
        )
        ShoppingCartList(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
        )
        Row(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { viewModel.emptyShoppingCart() },
            ) {
                Text(text = "TØM HANDLEKURV")
            }
            Spacer(modifier = Modifier.width(16.dp))
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
                },
            ) {
                Text(text = "SEND BESTILLING")
            }
        }
    }
}

@Composable
fun ShoppingCartList(navController: NavController, viewModel: ArtViewModel, modifier: Modifier) {
    val artPhotos = viewModel.dbShoppingCart.collectAsState(initial = emptyList())
    LazyColumn(
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
    ) {
        items(count = artPhotos.value.size) { index ->
            val artPhoto = artPhotos.value[index]
            ShoppingCartPhotoCard(artPhoto = artPhoto, viewModel = viewModel)
            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
fun ShoppingCartPhotoCard(artPhoto: ShoppingCart, viewModel: ArtViewModel) {
    val constraints = ConstraintSet {
        val photoComposable = createRefFor("photo_composable")
        val titleComposable = createRefFor("title_composable")
        val frameTypeComposable = createRefFor("frame_type_composable")
        val imageSizeComposable = createRefFor("image_size_composable")
        val removeImageButtonComposable = createRefFor("remove_image_button_composable")
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
            bottom.linkTo(parent.bottom, 0.dp)
            top.linkTo(parent.top, 0.dp)
        }
        constrain(numberOfImagesComposable) {
            end.linkTo(addImageButtonComposable.start, 8.dp)
            top.linkTo(parent.top, 0.dp)
            bottom.linkTo(parent.bottom, 0.dp)
        }
        constrain(addImageButtonComposable) {
            end.linkTo(parent.end, 8.dp)
            top.linkTo(parent.top, 0.dp)
            bottom.linkTo(parent.bottom, 0.dp)
        }
    }
    Box(
        modifier = Modifier
            .padding(0.dp)
            .height(90.dp)
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
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                    )
                    .padding(1.dp)
                    .border(
                        3.dp,
                        viewModel.getBorderColor(artPhoto.frameType)
                    )
                    .padding(3.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                    )
            )
            Row(modifier = Modifier
                .layoutId("title_composable"),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ){
                Text(
                    text = artPhoto.imageTitle,
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
                onClick = { viewModel.removePhotoFromDb(artPhoto) },
                modifier = Modifier
                    .layoutId("remove_image_button_composable")
                    .height(50.dp)
                    .width(50.dp)
                ) {
                    Text(
                        text = "-",
                        fontSize = 24.sp
                    )
                }
            Text(
                text = artPhoto.amount.toString(),
                modifier = Modifier
                    .layoutId("number_of_images_composable")
                    .width(20.dp),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { viewModel.addToBasket(artPhoto, true)},
                modifier = Modifier
                    .layoutId("add_image_button_composable")
                    .height(50.dp)
                    .width(50.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp
                )
            }
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