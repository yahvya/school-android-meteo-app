package yahvya.meteo_app.views

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import yahvya.meteo_app.components.SearchbarComponent
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import yahvya.meteo_app.components.AppMessageComponent
import yahvya.meteo_app.components.FavoritePreviewComponent
import yahvya.meteo_app.components.GetLocationComponent
import yahvya.meteo_app.components.WeatherPreviewComponent
import yahvya.meteo_app.viewmodels.HomeViewModel
import yahvya.meteo_app.viewmodels.WeatherDetailsViewModel

/**
 * @brief home page
 * @param modifier modifier
 * @param lookAllFavoritesClick event on click to see all favorites
 * @param onWeatherPreviewClick action on details click
 * @param homeViewModel home view model
 * @param weatherDetailsViewModel weather details view model
 * @param context context
 */
@Composable
fun HomeView(
    modifier:Modifier,
    lookAllFavoritesClick: () -> Unit,
    onWeatherPreviewClick: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(),
    weatherDetailsViewModel: WeatherDetailsViewModel = viewModel(),
    context: Context = LocalContext.current
){
    val proposals = homeViewModel.getProposalsState().value
    val favorites = homeViewModel.getFavoritesState()
    val userMessageState = homeViewModel.getUserMessageState()

    homeViewModel.loadFavorites()

    LazyColumn(
        modifier= modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        item{
            AppMessageComponent(messageState = userMessageState)
        }

        item{
            Text(
                text = "Ma méteo".uppercase(),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom= 20.dp,top= 5.dp)
            )
        }

        // searchbar
        item{
            val searchState = homeViewModel.getResearchState()

            // search while user type
            LaunchedEffect(searchState.value) {
                homeViewModel.searchProposals(search = searchState.value,context= context)
            }

            SearchbarComponent(
                modifier= Modifier.fillMaxWidth(),
                textFieldValue = searchState,
                searchbarPlaceholder = "Rechercher une ville"
            )
        }

        // search from location
        item{
            GetLocationComponent(
                modifier = Modifier.fillMaxWidth(),
                onLocationGet = { location ->
                    if(location != null)
                        homeViewModel.searchFromLocation(longitude = location.longitude, latitude = location.latitude,context= context)
                    else
                        userMessageState.value = "Echec de récupération de la localisation :("
                },
                onDeny = {
                    userMessageState.value = "Vous avez bloqué l'utilisation de la location. Rendez-vous dans les paramètres ;)"
                }
            )
        }

        // search propositions / results
        if(proposals.isNotEmpty()){
            item{
                Text(
                    text= "Résultats",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(items= proposals){ item ->
                WeatherPreviewComponent(
                    modifier = Modifier.fillMaxWidth(),
                    weatherDto = item,
                    onViewClicked = {
                        weatherDetailsViewModel.weatherDto.value = item
                        onWeatherPreviewClick()
                    }
                )
            }
        }

        // certain favorites
        if(favorites.isNotEmpty()){
            item {
                Text(
                    text = "Vos favoris",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(items= favorites){ item ->
                val favoriteState = remember{mutableStateOf(item.isFavorite)}

                LaunchedEffect(favoriteState.value) {
                    if(!favoriteState.value)
                        homeViewModel.removeInFavorites(item)
                }

                FavoritePreviewComponent(
                    modifier = Modifier.fillMaxWidth(),
                    weatherDto = item,
                    isFavorite = favoriteState,
                    onViewClicked = {
                        weatherDetailsViewModel.weatherDto.value = item
                        onWeatherPreviewClick()
                    }
                )
            }

            item{
                // see more favorites
                Text(
                    text= "Voir plus ...",
                    modifier= Modifier.clickable {
                        lookAllFavoritesClick()
                    },
                    color = Color(red=13,green=110,blue=253),
                    fontSize = 18.sp,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
@Preview
fun HomeViewPreview(){
    HomeView(
        modifier = Modifier,
        lookAllFavoritesClick = {},
        onWeatherPreviewClick = {}
    )
}