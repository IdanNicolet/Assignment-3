<?php
$servername = "mysql11.000webhost.com";
$username = "a8510183_root";
$password = "Password1";
$db = "a8510183_clients";
$DEBUG = TRUE;

if (!$link = mysql_connect($servername , $username , $password)) {
    echo 'Could not connect to mysql';
    exit;
}

if (!mysql_select_db($db, $link)) {
    echo 'Could not select database';
    exit;
}
// http://matchrace.net16.net/insertClient.php?table=clients&Latitude=11&Longitude=10&Pressure=0&Azimuth=0&Bearing=0&Information=user1_bbb_1&Event=1
$mode = $_GET["table"];
$lat = $_GET["Latitude"];
$lon = $_GET["Longitude"];
$speed = $_GET["Pressure"];
$azi = $_GET["Azimuth"];
$ber = $_GET["Bearing"];
$info = explode("_", $_GET["Information"]);
$user = $info[0];
$password = $info[1];
$event = $_GET["Event"];

if ($mode == "clients")
// login
{
	$user = str_replace ("Cord", "", $user);
	$user = str_replace ("Sailor", "", $user);

	$sql = 'SELECT * FROM clients WHERE name=\''.$user.'\'';
	$result = mysql_query ($sql) or die(mysql_error());
	$row = mysql_fetch_assoc($result);
	if ($row && $row["password"] == $password)
		echo "{\"positions\":[{\"event\":\"".$row["event"]."\"}]}";

} else if ($mode == "clientsBuoys") {
	$sql = 'SELECT * FROM events WHERE event=\''.$event.'\'';
	$result = mysql_query ($sql) or die(mysql_error());
	$row = mysql_fetch_assoc($result);
	if (!$row)
		die("ERROR no such Event");
	else
	{
		echo "{\"Buoys\":[<br>";

		for ($i = 1; $i <= 10; $i++)
		{
			$lati = "lat".$i;
			$loni = "lon".$i;

			if ($row[!$lati]) break;
			echo ("
			{
			\"lat\":\"".    $row[$lati]    ."\", \"lon\":\"".    $row[$loni]    ."\"}");

			if ($i < 10 && $row["lat".($i+1)])
				echo ",<br>";
		}
		
		echo "<br>]}";
	
	}

	if ($DEBUG)
	{
		echo "<br>";
		print_r($array);
		echo $sql;
	}


} else {

	$sql = 'SELECT * FROM cords';
	$result = mysql_query ($sql) or die(mysql_error());
	
	echo "{\"positions\":[<br>";
	for ($i = 0; $i < mysql_num_rows($result)-1; $i++)
	{
		$row = mysql_fetch_assoc($result);
		echo ("
		{
		\"ip\":\"11.11.11.11\",
		\"time\":\"".$row["time"]."\",
		\"lat\":\"".$row["lat"]."\",
		\"lon\":\"".$row["lon"]."\",
		\"alt\":\"-1\",
		\"bearing\":\"".$row["bearing"]."\",
		\"OrientX\":\"-1\",
		\"OrientY\":\"-1\",
		\"azimuth\":\"".$row["azi"]."\",
		\"pressure\":\"-1\",
		\"info\":\"".$row["name"]."\",
		\"event\":\"".$row["event"]."\"
		},	
		<br>");
	}
	
	
	$row = mysql_fetch_assoc($result);
	echo ("
		{
		\"ip\":\"11.11.11.11\",
		\"time\":\"".$row["time"]."\",
		\"lat\":\"".$row["lat"]."\",
		\"lon\":\"".$row["lon"]."\",
		\"alt\":\"-1\",
		\"bearing\":\"".$row["bearing"]."\",
		\"OrientX\":\"-1\",
		\"OrientY\":\"-1\",
		\"azimuth\":\"".$row["azi"]."\",
		\"pressure\":\"-1\",
		\"info\":\"".$row["name"]."\",
		\"event\":\"".$row["event"]."\"
		}	
		<br>");
	echo "]}";
	
	

}

?>				