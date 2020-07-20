const https = require('https');
const express = require('express');
const router = express.Router();
const app = express();
const googleTrends = require('google-trends-api');

const nyt_api_key = "PnjspRzKJvEfFV6gUyHrwmz6DT7oEgyD";
const guardian_api_key = "4a4c4b72-2b40-492f-af31-b4627e133bae";



router.get("/get_trends/:keyword", function(req, res){
	res.setHeader('Content-Type', "application/json");
  	res.header("Access-Control-Allow-Origin", "*");
	res.header("Access-Control-Allow-Headers", "X-Requested-With");
	googleTrends.interestOverTime({keyword: req.params.keyword, startTime: new Date("2019-06-01")})
		.then(function(results){
  			res.send(JSON.parse(results));
		})
		.catch(function(err){
  			console.error('Oh no there was an error', err);
		});
});


router.get("/get_article/:source_name", function(req, res){
	res.setHeader('Content-Type', "application/json");
  	res.header("Access-Control-Allow-Origin", "*");
	res.header("Access-Control-Allow-Headers", "X-Requested-With");
	if(req.params.source_name == "guardian"){
		https.get("https://content.guardianapis.com/"+req.query.identifier+"?api-key="+guardian_api_key+"&show-blocks=all", response => {
		let data = "";
		response.on("data", chunk => {
			data += chunk;
		});
		response.on("end", () => {
			res.send(JSON.parse(data));
		});
	}).on("error", err => {
			console.log("Error: "+ err.message);
		});
	}
	else if(req.params.source_name == "nyt"){
		https.get('https://api.nytimes.com/svc/search/v2/articlesearch.json?fq=web_url:(%22'+req.query.identifier+'%22)&api-key='+nyt_api_key, response => {
		let data = "";
		response.on("data", chunk => {
			data += chunk;
		});
		response.on("end", () => {
			res.send(JSON.parse(data));
		});
	}).on("error", err => {
			console.log("Error: "+ err.message);
		});
	}
	else{
		res.send("Invalid source name");
	}
});


router.get("/get_article/:source_name", function(req, res){
	res.setHeader('Content-Type', "application/json");
  	res.header("Access-Control-Allow-Origin", "*");
	res.header("Access-Control-Allow-Headers", "X-Requested-With");
	if(req.params.source_name == "guardian"){
		https.get("https://content.guardianapis.com/"+req.query.identifier+"?api-key="+guardian_api_key+"&show-blocks=all", response => {
		let data = "";
		response.on("data", chunk => {
			data += chunk;
		});
		response.on("end", () => {
			res.send(JSON.parse(data));
		});
	}).on("error", err => {
			console.log("Error: "+ err.message);
		});
	}
	else if(req.params.source_name == "nyt"){
		https.get('https://api.nytimes.com/svc/search/v2/articlesearch.json?fq=web_url:(%22'+req.query.identifier+'%22)&api-key='+nyt_api_key, response => {
		let data = "";
		response.on("data", chunk => {
			data += chunk;
		});
		response.on("end", () => {
			res.send(JSON.parse(data));
		});
	}).on("error", err => {
			console.log("Error: "+ err.message);
		});
	}
	else{
		res.send("Invalid source name");
	}
});


router.get("/search/:source_name/:keyword", function(req, res){
	res.setHeader('Content-Type', "application/json");
  	res.header("Access-Control-Allow-Origin", "*");
  	res.header("Access-Control-Allow-Headers", "X-Requested-With");
	if(req.params.source_name == "guardian"){
		https.get("https://content.guardianapis.com/search?q="+req.params.keyword+"&api-key="+guardian_api_key+"&show-blocks=all", response => {
		let data = "";
		response.on("data", chunk => {
			data += chunk;
		});
		response.on("end", () => {
			res.send(JSON.parse(data));
		});
	}).on("error", err => {
			console.log("Error: "+ err.message);
		});
	}
	else if(req.params.source_name == "nyt"){
		https.get("https://api.nytimes.com/svc/search/v2/articlesearch.json?q="+req.params.keyword+"&api-key="+nyt_api_key, response => {
		let data = "";
		response.on("data", chunk => {
			data += chunk;
		});
		response.on("end", () => {
			res.send(JSON.parse(data));
		});
	}).on("error", err => {
			console.log("Error: "+ err.message);
		});
	}
	else{
		res.send("Invalid source name");
	}
});


router.get("/section_news/:source_name/:section_name", function(req, res){
	res.setHeader('Content-Type', "application/json");
  	res.header("Access-Control-Allow-Origin", "*");
	res.header("Access-Control-Allow-Headers", "X-Requested-With");
	if (req.params.source_name === "guardian"){
		var section_name = req.params.section_name;
		if (req.params.section_name == "sports"){
			section_name = "sport";
		}
		if(section_name !== "home"){
			https.get("https://content.guardianapis.com/search?api-key="+guardian_api_key+"&section="+section_name+"&show-blocks=all", response => {
			let data = "";
			response.on("data", chunk => {
				data += chunk;
			});
			response.on("end", () => {
				res.send(JSON.parse(data));
			});
		}).on("error", err => {
				console.log("Error: "+ err.message);
			});
		}
		else {
			https.get("https://content.guardianapis.com/search?order-by=newest&show-fields=starRating,headline,thumbnail,short-url&api-key=" + guardian_api_key, response => {
			let data = "";
			response.on("data", chunk => {
				data += chunk;
			});
			response.on("end", () => {
				res.send(JSON.parse(data));
			});
		}).on("error", err => {
				console.log("Error: "+ err.message);
			});
		}
		
	}
	else if(req.params.source_name == "nyt"){
		https.get("https://api.nytimes.com/svc/topstories/v2/"+req.params.section_name+".json?api-key="+nyt_api_key, response => {
		let data = "";
		response.on("data", chunk => {
			data += chunk;
		});
		response.on("end", () => {
			res.send(JSON.parse(data));
		});
	}).on("error", err => {
			console.log("Error: "+ err.message);
		});
	}
	else{
		res.send("Invalid source name");
	}
});


app.use(router);

app.listen(8080, function(){
	console.log("Active on Port 8080 now!");
});
module.exports = app;