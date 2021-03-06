<!DOCTYPE html>
<html lang="en">
<head>
<!-- Required meta tags always come first -->
<meta charset="utf-8">
<Meta http-equiv="Content-Type" Content="text/html; Charset=utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- <meta http-equiv="x-ua-compatible" content="ie=edge"> -->
<!-- Bootstrap CSS -->
<link rel="stylesheet"
	href="/static/js/bootstrap/dist/css/bootstrap.css">


<!-- jQuery first, then Bootstrap JS. -->

</head>
<body>
 	<#include "../admin/topMenu.ftl">

	
    <div class="jumbotron" >
	  <div class="container" style="text-align: center">
	  					<h1 class="display-4" style="text-align: center;">ifreeshare分类创建</h1>
					  <p  style="text-align: center;">本网站所有数据均来自于网络上的公开数据并致力于对其进行免费的分享。</p>
					  <hr class="m-y-md">
	  </div>
	</div>

	<div class="container">
		<form action="/admin/classif/create/" method="post" >
			
			<div class="form-group row">
				<label for="example-text-input" class="col-xs-2 col-form-label">名称:</label>
					<input id="id_name" name="name" class="form-control" type="text"  
						id="example-text-input">
			</div>
			
			<div class="form-group row">
				<label for="example-text-input" class="col-xs-2 col-form-label">父类:</label>
					<input id="id_parent" name="parent" class="form-control" type="text"  
						id="example-text-input">
			</div>
			
			<div class="form-group row">
				<label for="example-text-input" class="col-xs-2 col-form-label">别名:</label>
					<input id="id_alias" name="alias" class="form-control" type="text"   
						id="example-text-input">
			</div>
			
			<div class="form-group row">
				<label for="example-text-input" class="col-xs-2 col-form-label">关键字:</label>
					<input id="id_keywords" name="keywords" class="form-control" type="text"   
						id="example-text-input">
			</div>
			
			<div class="form-group row">
				<label for="example-text-input" class="col-xs-2 col-form-label">标识:</label>
					<input id="id_tags" name="tags" class="form-control" type="text"   
						id="example-text-input">
			</div>
			
			<div class="form-group row">
				<label for="example-text-input" class="col-xs-2 col-form-label">描述:</label>
					<input id="id_description" name="description" class="form-control" type="text"   
						id="example-text-input">
			</div>
			
			<div class="form-group row">
		      <div class="offset-sm-2 col-sm-10">
		        <button type="submit" class="btn btn-primary">创建</button>
		      </div>
		    </div>
		</form>

	</div>
	
		 <!-- include  footer -->
		   <#include "../../footer.ftl">
	
	
</body>

<script src="/static/js/jquery/jquery.min.js"></script>
<script
	src="/static/js/bootstrap/dist/js/tether.min.js"
	></script>

<script src="/static/js/bootstrap/dist/js/bootstrap.js"></script>
</html>