<%@ page import="java.util.List" %>
<%@ page import="com.macklive.objects.Team" %>
<%@ page import="com.macklive.storage.DataManager" %>
<%
	DataManager dm = DataManager.getInstance();
	List<Team> teams = dm.getTeams();
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title>Team Management</title>
	<link rel="Stylesheet" href="/css/console.css" type="text/css"/>
	<style>
		body {
			min-width: 500px;
		}
	</style>
	<script language='javascript' type="text/javascript"
			src='/api/js/console'></script>
	<script language='javascript' type="text/javascript">
		var teamManager;
		$j(document).ready(function () {
			teamManager = new TeamManagerConsole("teamContainer");
			teamManager.render();
		});
	</script>
</head>

<body>
<div>
	<img class="]floatL headerImage" src="/images/mllogo.png">
	<h1 style="position: absolute; top: 10px; right: 25px; color: white;">
		Team Management</h1>
</div>
<div id='teamContainer' class='userBoxStyle'
	 style="height: calc(100% - 106px); width: calc(100% - 70px); padding: 15px; margin: auto; margin-top: 10px;">
	<div>
		<label for="teamPicker">Team: </label>
		<select id="teamPicker">
			<option>[Add New]</option>
			<% for (Team t : teams) { %>

			<option id='<%= t.getKey().getId() %>'><%= t.getName() %>
			</option>

			<% } %>
		</select>
	</div>
	<div>
		<img id="teamLogo" src="/images/placeholderImage.png"
			 style="margin-top: 15px; margin-left:10px; height: 100px; width: 100px; border: 1px solid black;"/>
		<div style='margin-left:10px; margin-top: 10px;'>
			<input id='uploadButton' type='file' accept='image/*' value='Choose Image...'>
		</div>
	</div>
	<div style="position: absolute; left:150px; top:50px; white-space: nowrap;">
		<label for='teamNameInput' style="font-size: 20px;">Team Name:</label>
		<input id='teamNameInput' type="text" style="font-size:20px;" placeholder="Team Name">
		<br><br>
		<label for='teamAbbrInput' style="font-size: 20px;">Short Name:</label>
		<input id='teamAbbrInput' type="text" style="font-size:20px;" placeholder="Short Name">
	</div>
	<div class='absoluteBottomRight'>
		<input id='deleteButton' class='red' type='button' value='Delete' disabled>
		<input id='saveButton' class='action' style='margin-left:10px;' type='button' value='Save' disabled>
	</div>
</div>
</body>
</html>
