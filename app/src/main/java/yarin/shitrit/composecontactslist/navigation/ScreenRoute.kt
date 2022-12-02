package yarin.shitrit.composecontactslist.navigation

/**
 * Class for providing routes to NavController
 */
sealed class ScreenRoute(val route: String) {
    object ContactsListScreen : ScreenRoute("ContactsListScreen")
    object DetailedContactScreen : ScreenRoute("DetailedContactScreen")
}
