
![face lock showcase 2](https://user-images.githubusercontent.com/29645585/40404184-7447af28-5e24-11e8-908f-b2dd2822b4d9.png)

# Face Lock

Face Lock is an Android app/service uses facial recognition to lock the device when an unauthorized face is using it. The faces of close ones can be added to the accepted list and the feature can be toggled on and off to provide our users with utmost versatility for their unique needs. Further, if the phone has been stolen and locked, the user will immediately receive an email notification and will be able to log into our companion website for further support. On the website, users will be able to access their phone's location services to track down the phone and see updated photos taken by the front camera of the potential thief. Not only does this technology protect data and increase the chances of reclaiming a stolen phone, its proliferation will discourage phone theft.

Face Lock was started at HackPrinceton Fall 2017 and can be found on Devpost [here.](https://devpost.com/software/swiper-no-swiping)

Our project represented a unique challenge of having equal importance in web and mobile development - which were integrated through Amazon Web Services. On a set background task interval, Face Lock uses the front facing camera and AWS Rekognition to check if the user is authorized to use the device. If the user is unauthorized, their photo is uploaded to AWS S3 and sent to the device's owner through text and email with AWS SNS. Simultaniously, Face Lock begins rapid location tracking and uploads the phone's coordinates to DynamoDB.

This data stored on S3 and DynamoDB is then pulled down to our Node.js + EJS web app where the user can track their devices on our embedded Bing Maps and can view all previously identified intruders.

The website is currently being updated to an Angular + Node.js app but the older version can be viewed from our previous release.
