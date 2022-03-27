# Track
In this challenge I have developed an that get user location and show the available vehicles with nearest vehicle detail.
To solve this challenge I have used MVI (Model, View, Intent)architecture with clean architecture approach. The whole code base is divided into 5 modules app, core, 
feature-track, domain-track and data-track. The app module contains only the app related code. 

The core module contains the code which is common for the whole project. If we extend any feature then we can reuse the code of core module. So the most 
common functionalities are placed into core module.

feature-track module contains the code related to the feature. We track the vehicles in this feature and now this is the only feature that app contains. 
The feature module depends on its domain module. The feature module contains the code of presentaton layer in the clean code approach. It contains the ui,
view model, ui data model , util and mapper classes. View model is dependent on use case for business logic. In MVI architecture, the user interactions are 
considered as events or intent which change the model and recreate immutable states which is shown in the view. 

The domain layer is responsible the business logic. The use case contains the business logic. The domain module is agnostic of any other module. It does not
depend on any other module. In this feature use case just get the vehicles from repository and calculate distance of vehicles from user location and place 
the nearest vehicle at the 1st position of the vehicle list. 

The data layer is responsible for providing the data from different sources. For this app this layer will collect data from the backend server and provide 
them to domain module. So the data flow goes like data -> domain -> presentation. 
The technologies and tools I have used are given below:
- kotlin
- Hilt
- Retrofit
- Kotlinx serialization
- Coroutines (flow)
- Jetpack
- Room
- Junit
- mockk
- turbine
- chucker
- detekt
- ktlint
