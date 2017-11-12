

var authenticationData = {
        Username : 'username',
        Password : 'password',
    };
    var authenticationDetails = new AWSCognito.CognitoIdentityServiceProvider.AuthenticationDetails(authenticationData);
    var poolData = { UserPoolId : 'us-east-1_TcoKGbf7n',
        ClientId : '4pe2usejqcdmhi0a25jp4b5sh3'
    };
    var userPool = new AWSCognito.CognitoIdentityServiceProvider.CognitoUserPool(poolData);
    var userData = {
        Username : 'username',
        Pool : userPool
    };
    var cognitoUser = new AWSCognito.CognitoIdentityServiceProvider.CognitoUser(userData);
    cognitoUser.authenticateUser(authenticationDetails, {
        onSuccess: function (result) {
            console.log('access token + ' + result.getAccessToken().getJwtToken());
            /*Use the idToken for Logins Map when Federating User Pools with Cognito Identity or when passing through an Authorization Header to an API Gateway Authorizer*/
            console.log('idToken + ' + result.idToken.jwtToken);
        },

        onFailure: function(err) {
            alert(err);
        },

    });
