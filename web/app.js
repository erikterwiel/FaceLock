const express = require("express");
const path = require("path");

const app = express();

const users = require("./routes/users");

var port = 3000;

app.use(express.static(path.join(__dirname, "public")))

app.use("/users", users);

app.listen(port, () => {
    console.log("Server started on port: " + port);
});
