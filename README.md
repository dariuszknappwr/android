# Introduction

Android app with functionality of adding ingredients that user has, then search for recipes including those ingredients.
The main funcionality includes:
* **Adding ingredients**: The user can add ingredients that are saved in the local database. The user can choose from 5000 most popular ingredients that supports external API.
* **Scanning the barcode** or typing the product name. After product is scanned, another external API replaces the barcode with the name of the product, then it tries to match to available ingredients.
* **Searching Recipe**: User can search for recipes based on various criteria, such as recipe name, cuisine, ingredient intolerances and different diets. It is possible to find recipes that are best suited to the ingredients user have added. The search is performed by queries to an external API.
* **Viewing Favorite Recipes**: User can add a recipe to favorites while searching. The recipe is also saved to the local database.
* **Viewing full recipe details**. The first display of a recipe collects data from the API, a each subsequent one from the database.

# Technologies
* Application written in Kotlin.
* Used barcode scanning library was used com.journeyapps:zxing-android-embedded:4.3.0.
* Used the Room library for saving data in a local SQLite database.
* Used the Spoonacular API was used to search for recipes from the Internet.
* Used the Openfoodfacts API to generate product name from bar code.


# Application
## Add ingredients view

<div align="center">
  
![obraz](https://github.com/dariuszknappwr/android/assets/127883702/2c333f1a-3f2c-4211-a63b-e4cd5ab20b84) ![obraz](https://github.com/dariuszknappwr/android/assets/127883702/1cf415d2-3dc8-4647-a498-6bdfe2f35563)
  
</div>

After typing first letters of the ingredient, a window appears with the option to select the ingredient.
User can add only those ingredients that appear on the list. The ingredients come from the list of the most popular Spoonacular API ingredients.
From this activity user can proceed to scanning products.

## Product scanning view
<div align="center">

![obraz](https://github.com/dariuszknappwr/android/assets/127883702/2781a347-c7e8-46d3-baa8-aa37abdf1a90) ![obraz](https://github.com/dariuszknappwr/android/assets/127883702/72990c09-6630-4459-aa33-f7ed78242018)

</div>

After scanning the product, a query is sent to the API, which translates the barcode into a name of the product.
This name is complex, so it captures keywords that may be put into list of ingredients. Possible ingredients are displayed in the form of a Spinner.

## Recipe search view
<div align="center">

![obraz](https://github.com/dariuszknappwr/android/assets/127883702/307819ea-4c3f-4995-ab40-e22f9004c9b5) ![obraz](https://github.com/dariuszknappwr/android/assets/127883702/bba0cdc4-29c6-4d1f-9fd9-5ed276e66d30)

</div>


This activity allows user to search for recipes using the Spoonacular API.
All parameters are optional.
Checking the "Only owned ingredients" option will read ingredients user added to list and will try to choose recipes that contain ingredients owned by the user.

## Favorite recipe list and recipe screen
<div align="center">
  
![obraz](https://github.com/dariuszknappwr/android/assets/127883702/63fe84a5-f159-490d-993f-9113eaf850f0) ![obraz](https://github.com/dariuszknappwr/android/assets/127883702/934fbc13-4418-4788-a19d-eaf7b37c3fc7)

</div>

This activity shows recipes added to favorites.
Recipes are fetched from local database.
