var http = require('http');
var fs = require('fs');
var express = require('express');

var app=express();
app.use('/static',express.static(__dirname+'/static'));
app.set('view engine','ejs');
app.set('views','./views');


/*
fs.readFile('./index.html', function (err, html) {
    if (err) {
        throw err; 
    }       
    http.createServer(function(request, response) {  
        response.writeHeader(200, {"Content-Type": "text/html"});  
        response.write(html);  
        response.end();  
    }).listen(8000);
});

*/


var AWS = require("aws-sdk");
AWS.config.update({
  region: "us-east-1",
  endpoint: "http://localhost:8000"
});

AWS.config.loadFromPath('config.json');

var docClient = new AWS.DynamoDB.DocumentClient()

var long;
    var lat;
    

app.get('/',function(req,res) {
        var params = {
        AttributesToGet:[
            "Latitude", "Longitude",""
        ],
        TableName: "Usernames",
        Key:{
            "Username": "cefd63b1-d378-4141-b986-31ba2c058a21",
        }
    };
    

    


    docClient.get(params, function(err, data) {
        if (err) {
            console.error("Unable to create table. Error JSON:", JSON.stringify(err, null, 3));
        } else {
            console.log(typeof data);
            console.log("Created table. Table description JSON:", JSON.stringify(data, null, 2));
            long=data.Item.Longitude;
            lat=data.Item.Latitude;
            console.log([lat,long]);
            res.render('index',{
                coords: [lat,long],        
            });

        }
    });
    
    
});
    





server = app.listen(8000,function() {
    console.log("connected");
});
