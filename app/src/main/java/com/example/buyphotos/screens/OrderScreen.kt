package com.example.buyphotos.screens

import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.layoutId
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.buyphotos.database.ArtPhotoApplication
import com.example.buyphotos.model.ArtViewModel
import com.example.buyphotos.model.ArtViewModelFactory
import com.example.buyphotos.network.ArtPhoto
import com.example.buyphotos.network.ArtPhotoApi
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Border
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.buyphotos.database.ShoppingCart
import com.example.buyphotos.navigation.BottomBarScreen

@Composable
fun RadioButtons(
    radioTitle: String,
    radioOptions: List<String>,
    viewModel: ArtViewModel,
    onOptionSelected: (String) -> Unit,
    onSelectionChanged: () -> Unit
) {
    val defaultOption = radioOptions[0]
    val (selectedOption, setSelectedOption) = rememberSaveable { mutableStateOf(defaultOption) }
    Column {
        Text(
            text = radioTitle
        )
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = {
                            onOptionSelected(text)
                            setSelectedOption(text)
                            onSelectionChanged()
                        }
                    ),

                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    modifier = Modifier
                        .size(40.dp),
                    selected = (text == selectedOption),
                    onClick = {
                        onOptionSelected(text)
                        setSelectedOption(text)
                              },
                )
                Text(
                    text = text,
                )
            }
        }
    }
}

fun getBorderColor(frame: String?): Color {
    val brown = Color(150, 75, 0)
    return when (frame) {
        "Treramme" -> brown// Change this to the desired color for the Treramme option
        "Sølvramme" -> Color.Gray // Change this to the desired color for the Sølvramme option
        "Gullramme" -> Color.Yellow // Change this to the desired color for the Gullramme option
        else -> Color.Black
    }
}

@Composable
fun CustomTextField(onNumberOfPhotosChanged: (Int) -> Unit) {
    var text by remember { mutableStateOf("1") }
    val focusRequester = remember { FocusRequester() }
    var hasBeenFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onNumberOfPhotosChanged(it.toIntOrNull() ?: 1)
                        },
        label = { Text("Antall") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .layoutId("number_of_photos_composable")
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !hasBeenFocused) {
                    text = ""
                    hasBeenFocused = true
                }
            },
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
    )
}

@Composable
fun OrderScreen(imageId: Int, viewModel: ArtViewModel) {
    val frame by viewModel.frame.observeAsState("Treramme")
    val imageSize by viewModel.chosenImageSize.observeAsState("Liten")
    val price by viewModel.price.observeAsState()
    val numberOfPhotos = remember { mutableStateOf(1) }
    val navController = rememberNavController()

    val constraints = ConstraintSet {
        val photoComposable = createRefFor("photo_composable")
        val artistNameComposable = createRefFor("artist_name_composable")
        val artistEmailComposable = createRefFor("artist_email_composable")
        val radioComposable = createRefFor("radio_composable")
        val priceComposable = createRefFor("price_composable")
        val numberOfPhotosComposable = createRefFor("number_of_photos_composable")
        val subTotalPriceComposable = createRefFor("subtotal_price_composable")
        constrain(photoComposable) {
            top.linkTo(parent.top, 16.dp)
            start.linkTo(parent.start, 64.dp)
            end.linkTo(parent.end, 64.dp)
            width = Dimension.fillToConstraints
        }
        constrain(artistNameComposable) {
            top.linkTo(photoComposable.bottom, 16.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(artistEmailComposable) {
            top.linkTo(artistNameComposable.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
        constrain(radioComposable) {
            top.linkTo(artistEmailComposable.bottom, 16.dp)
            start.linkTo(photoComposable.start)
            end.linkTo(photoComposable.end)
            width = Dimension.fillToConstraints
        }
        constrain(priceComposable) {
            top.linkTo(radioComposable.bottom, 16.dp)
            start.linkTo(photoComposable.start)
            end.linkTo(photoComposable.end)
            width = Dimension.fillToConstraints
        }
        constrain(numberOfPhotosComposable) {
            top.linkTo(priceComposable.bottom, 16.dp)
            start.linkTo(priceComposable.start)
            end.linkTo(priceComposable.end)

        }
        constrain(subTotalPriceComposable) {
            top.linkTo(numberOfPhotosComposable.bottom, 16.dp)
            start.linkTo(priceComposable.start)
            end.linkTo(priceComposable.end)

        }
    }
    val artPhoto = remember { mutableStateOf<ArtPhoto?>(null) }
    LaunchedEffect(imageId) {
        val photo = viewModel.photos.value?.find { it.id.toInt() == imageId }
        artPhoto.value = photo
        viewModel.setFrame("Treramme")
        viewModel.setImageSize("Liten")
    }
    ConstraintLayout(
        constraints,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        when (val photo = artPhoto.value) {
            null -> {
                Text(
                    text = "Loading...",
                    fontSize = MaterialTheme.typography.h3.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            else -> {
                Box(modifier = Modifier.layoutId("photo_composable")){
                    val imageModifier = when (imageSize) {
                        "Liten" -> Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .scale(0.6f)
                        "Medium" -> Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .scale(0.8f)
                        else -> Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)
                    }
                    Image(
                        painter = rememberAsyncImagePainter(photo.url),
                        contentDescription = null,
                        modifier = imageModifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                            )
                            .padding(2.dp)
                            .border(
                                10.dp,
                                getBorderColor(frame)
                            )
                            .padding(10.dp)
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                            )
                    )
                }
                Box (
                    modifier = Modifier
                        .layoutId("artist_name_composable")
                ) {
                    Text(
                        text = viewModel.artistName.value!!,
                        textAlign = TextAlign.Center
                    )
                }
                Box (
                    modifier = Modifier
                        .layoutId("artist_email_composable")
                ) {
                    Text(
                        text = viewModel.artistEmail.value!!,
                        textAlign = TextAlign.Center
                    )
                }
                Row(
                    modifier = Modifier
                        .layoutId("radio_composable"),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        RadioButtons(
                            "Rammetype:",
                            radioOptions = listOf("Treramme", "Sølvramme", "Gullramme"),
                            viewModel = viewModel,
                            onOptionSelected = { viewModel.setFrame(it) },
                            onSelectionChanged = { viewModel.calculatePrice() }
                        )
                    }
                    Spacer(modifier = Modifier.size(size = 40.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        RadioButtons("Bildestørrelse:",
                            radioOptions = listOf("Liten", "Medium", "Stort"),
                            viewModel = viewModel,
                            onOptionSelected = { viewModel.setImageSize(it) },
                            onSelectionChanged = { viewModel.calculatePrice() }
                            )
                    }
                }
                Text(
                    text = "Pris: ${price.toString()},-",
                    modifier = Modifier
                        .layoutId("price_composable"),
                    textAlign = TextAlign.Center
                )
                CustomTextField{ newValue -> numberOfPhotos.value = newValue}
                Text(
                    text = "Totalpris: ${(price?.times(numberOfPhotos.value)).toString()},-",
                    modifier = Modifier
                        .layoutId("subtotal_price_composable"),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        val shoppingCartItem = ShoppingCart(
                            imageId = photo.id,
                            imageUrl = photo.url,
                            imageTitle = photo.title,
                            frameType = frame,
                            imageSize = imageSize,
                            price = price ?: 0,
                            amount = numberOfPhotos.value
                        )
                        viewModel.addToBasket(shoppingCartItem)
                        println(shoppingCartItem)
                        navController.navigate("shopping_cart")
                        // EXCEPTION E PÅ GRUNN AV NAVCONTROLLER NAVIGATEN!
                    },
                    modifier = Modifier
                        .layoutId("add_to_basket_button")
                ) {
                    Text(text = "Legg til i handlekurv")
                }
            }
        }
    }
}