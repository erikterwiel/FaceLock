
![swiper no swiping showcase](https://user-images.githubusercontent.com/29645585/35464012-b194bab0-02c1-11e8-919d-9d45d6f4cdad.png)

# Swiper No Swiping

Swiper No Swiping is an Android app/service uses facial recognition to lock the device when an unauthorized face is using it. The faces of close ones can be added to the accepted list and the feature can be toggled on and off to provide our users with utmost versatility for their unique needs. Further, if the phone has been stolen and locked, the user will immediately receive an email notification and will be able to log into our companion website for further support. On the website, users will be able to access their phone's location services to track down the phone and see updated photos taken by the front camera of the potential thief. Not only does this technology protect data and increase the chances of reclaiming a stolen phone, its proliferation will discourage phone theft.

Swiper No Swiping was made at HackPrinceton Fall 2017 and can be found on Devpost [here.](https://devpost.com/software/swiper-no-swiping)

Our project represented a unique challenge of having equal importance in web and mobile development - which were integrated through Amazon Web Services. The core of our technology is based upon AWS Rekognition, AWS DynamoDB, AWS S3, AWS Cognito, and AWS SNS which respectively provided us the tools for image recognition, data storage, secure authentication, and detailed notifications, all of which are integral to the product. Then, we used Android Studio, Java, and XML to build the mobile app and connect the user to the tech. As for the website, we used Javascript, HTML, and CSS to develop the content and styling of the website, then used Node.JS to retrieve values from AWS DynamoDB and AWS S3 and connect it with the rest of our project.
