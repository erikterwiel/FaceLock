<!DOCTYPE HTML>
<?php
    session_start();
    $username = "user";
    $password = "password";
    if(isset($_SESSION['logged in']) && $_SESSION['logged in'] == true){
        header("Location: success.php");
    }
    if(isset($_POST['username']) && isset($_POST['password'])){
        if($_POST['username'] == $username && $_POST['password']==$password){
            $_SESSION['logged in']= true;
            header("Location: success.php");
        }
    }
?>

<html>
    <head>
        <meta charset="utf-8">
        <link href="main.css" type="text/css" rel="stylesheet">
    </head>
    <body>
        <form method="post" action="index.php">
            USERNAME<br/>
            <input type="text" name="username"><br/>
            PASSWORD<br/>
            <input type="password" name="password"><br/>
            <input type="submit" value="LOGIN">
        </form>
        <input type="file" id="file-chooser" />
    

</body>
</html>