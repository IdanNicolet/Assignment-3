<?php
$servername = "mysql11.000webhost.com";
$username = "a8510183_root";
$password = "Password1";
$db = "a8510183_clients";
$DEBUG = False;

if (!$link = mysql_connect($servername , $username , $password)) {
    echo 'Could not connect to mysql';
    exit;
}

if (!mysql_select_db($db, $link)) {
    echo 'Could not select database';
    exit;
}
// http://matchrace.net16.net/insertClient.php?table=clients&Latitude=11&Longitude=10&Pressure=0&Azimuth=0&Bearing=0&Information=user1_bbb_1&Event=1
$mode = make_safe($_GET["table"]);
$lat = make_safe($_GET["Latitude"]);
$lon = make_safe($_GET["Longitude"]);
$speed = make_safe($_GET["Pressure"]);
$azi = make_safe($_GET["Azimuth"]);
$ber = make_safe($_GET["Bearing"]);
$info = explode("_", $_GET["Information"]);
$user = make_safe($info[0]);
$password = make_safe($info[1]);
$event = make_safe($_GET["Event"]);

	$user = str_replace ("Cord", "", $user);
	$user = str_replace ("Sailor", "", $user);


if ($mode == "clients")
// login activity
{
	$sql = 'SELECT * FROM clients WHERE name=\''.$user.'\' AND event= '.$info[2];
	//echo "<br>".$sql."<br>";
	$result = mysql_query ($sql) or die(mysql_error());
	$row = mysql_fetch_assoc($result);
	if ($row && $row["password"] == $password)
		//echo "{\"positions\":[{\"event\":\"".$row["event"]."\"}]}";
                 echo "OK!";

} else if ($mode == "clientsBuoys") {
// Returnes all buoys
	$sql = 'SELECT * FROM events WHERE event=\''.$event.'\'';
	$result = mysql_query ($sql) or die(mysql_error());
	$row = mysql_fetch_assoc($result);
	if (!$row)
		die("ERROR no such Event");
	else
	{
		echo "{\"Buoys\":[";

		for ($i = 1; $i <= 10; $i++)
		{
			$lati = "lat".$i;
			$loni = "lon".$i;

			if ($row[!$lati]) break;
			echo ("
			{
			\"lat\":\"".    $row[$lati]    ."\", \"lon\":\"".    $row[$loni]    ."\"}");

			if (!$row["lat".($i+1)]) break;
			if ($i < 10 && $row["lat".($i+1)])
				echo ",";
		}
		
		echo "]}";
	
	}

	if ($DEBUG)
	{
		echo "<br>";
		print_r($array);
		echo $sql;
	}

} else if ($mode == "historyRace") {
// return all cords
	$sql = 
	'SELECT * 
	FROM cords 
	WHERE event ='.  $event  .' AND name != \''. $user .'\' 
	AND time = 
		(SELECT MAX(TIME) 
		FROM cords
		WHERE event =' . $event . '
		AND name !=  \''. $user .'\'
		GROUP BY name)';

	$result = mysql_query ($sql) or die(mysql_error());

	echo "{\"Positions\":[";
if (mysql_num_rows($result) != 0)
{
	//for ($i = 0; $i < mysql_num_rows($result); $i++)
	//{
		$row = mysql_fetch_assoc($result);
		echo ("
		{
		\"time\":\"".  $row["time"] ."\",
		\"lat\":\"".$row["lat"]."\",
		\"lon\":\"".$row["lon"]."\",
		\"azimuth\":\"".$row["azi"]."\",
		\"speed\":\"".$row["speed"]."\",
		\"name\":\"".$row["name"]."\",
		\"event\":\"".$row["event"]."\"
		}");
	//	if ($i != mysql_num_rows($result)-1) echo ",";
	//}
}
	echo "]}";	

} else if ($mode == "clientsUserCheck") {
	
		$sql = 'SELECT * FROM clients WHERE name =\'' .$user .'\' AND event = '. $info[2];
		$result = mysql_query ($sql) or die(mysql_error());
		if (mysql_num_rows($result) != 0)
			echo "NotOK";
		else
		{

			$sql = 'SELECT * FROM events WHERE event = '. $info[2];
			$result = mysql_query ($sql) or die(mysql_error());
			if (mysql_num_rows($result) == 0)
				echo "NoEvent";
			else
				echo "OK";
		}

} else {
// return all cords
	$sql = 'SELECT * FROM cords WHERE event ='.$event .' ORDER BY time DESC';
	$result = mysql_query ($sql) or die(mysql_error());

	echo "{\"Positions\":[";
	for ($i = 0; $i < mysql_num_rows($result); $i++)
	{
		$row = mysql_fetch_assoc($result);
		echo ("
		{
		\"time\":\"".  $row["time"] ."\",
		\"lat\":\"".$row["lat"]."\",
		\"lon\":\"".$row["lon"]."\",
		\"azimuth\":\"".$row["azi"]."\",
		\"speed\":\"".$row["speed"]."\",
		\"name\":\"".$row["name"]."\",
		\"event\":\"".$row["event"]."\"
		}");
		if ($i != mysql_num_rows($result)-1) echo ",";
	}
	echo "]}";	

}

function make_safe($variable) 
{
   $variable = strip_tags(mysql_real_escape_string(trim($variable)));
   return $variable; 
}

?>									