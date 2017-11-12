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
var u1;
var u2;
var u3;
    
    var url1 = new Array();
    var url2 = new Array();
    var src = new Array();
    var j = 0;
    var bucketname = "phoneprotectionpictures"
    var s3 = new AWS.S3();
    
    var param = {Bucket: bucketname};
    var count = 0;
    var bucketlength = 0;
    var equals = false;

app.get('/',function(req,res) {
        var params = {
        AttributesToGet:[
            "Latitude", "Longitude","Name"
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
            u1=data.Item.Name;
            console.log([lat,long]);
            
            
            s3.listObjects(param, function(err, data){
            var bucketContents = data.Contents;
                bucketlength = bucketContents.length;
                for (var i = 0; i < bucketlength; i++){
                  var urlParams = {Bucket: bucketname, Key: bucketContents[i].Key};
                    s3.getSignedUrl('getObject',urlParams, function(err, url){
                        url1 =  url.split("?");
                        url2 = url1[0].split("/");
                        src[i] = '"http://s3.amazonaws.com/phoneprotectionpictures/cefd63b1-d378-4141-b986-31ba2c058a21/Intruder/'+url2[url2.length-1]+'"';
                        console.log(src[i]);
                        if((i)===bucketlength){
                            res.render('index',{
                                srcarr: src,
                                coords: [lat,long],        
                        });
                        }
                    });
                    
                }

            }); 
            
        
        }
        
    });
    
    
    
    
    

});
    


server = app.listen(8000,function() {
    console.log("connected");
});
