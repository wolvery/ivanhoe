<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<?php require_once get_template_directory()."/BX_functions.php"; ?>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
	<title><?php bloginfo('name'); wp_title(); ?></title>
	<meta http-equiv="Content-Type" content="<?php bloginfo('charset'); ?>" />
	<meta name="generator" content="WordPress <?php bloginfo('version'); ?>" />
	<link rel="stylesheet" href="/stylesheets/spring_flavour.css" type="text/css" media="screen" />
	<link rel="stylesheet" href="/stylesheets/layout.css" type="text/css" media="screen" />
	<link rel="stylesheet" href="/stylesheets/lobby.css" type="text/css" media="screen" />
	<link rel="alternate" type="application/rss+xml" title="RSS 2.0" href="<?php bloginfo('rss2_url'); ?>" />
	<link rel="alternate" type="text/xml" title="RSS .92" href="<?php bloginfo('rss_url'); ?>" />
	<link rel="alternate" type="application/atom+xml" title="Atom 0.3" href="<?php bloginfo('atom_url'); ?>" />
	<link rel="pingback" href="<?php bloginfo('pingback_url'); ?>" />
	<?php /*comments_popup_script(520, 550);*/ ?>
	<?php wp_head();?>
</head>

<body><div id="container"<?php if (is_page() && !is_page("archives")) echo " class=\"singlecol\""; ?>>

<!-- header ................................. -->
<div id="header">
	<!-- <h1><a href="<?php bloginfo('url'); ?>"><?php bloginfo('name'); ?></a></h1> -->


</div> <!-- /header -->

<!-- navigation ................................. -->
<div id="navigation">

<!--
	<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="get">
		<fieldset>
			<input value="<?php echo wp_specialchars($s, 1); ?>" name="s" id="s" />
			<input type="submit" value="Go!" id="searchbutton" name="searchbutton" />
		</fieldset>
	</form>

	<ul>
-->
		<li<?php if (is_home()) echo " class=\"selected\""; ?>><a href="/">Home</a></li>
		<li><a href="/games">Games</a></li>
		<?php
		$pages = BX_get_pages();
		if ($pages) {
			foreach ($pages as $page) {
				$page_id = $page->ID;
   				$page_title = $page->post_title;
   				$page_name = $page->post_name;
   				if ($page_name == "archives") {
   					(is_page($page_id) || is_archive() || is_search() || is_single())?$selected = ' class="selected"':$selected='';
   					echo "<li".$selected."><a href=\"".get_page_link($page_id)."\">Archives</a></li>\n";
   				}
   				elseif($page_name == "about") {
   					(is_page($page_id))?$selected = ' class="selected"':$selected='';
   					echo "<li".$selected."><a href=\"".get_page_link($page_id)."\">About</a></li>\n";
   				}
   				elseif ($page_name == "contact") {
   					(is_page($page_id))?$selected = ' class="selected"':$selected='';
   					echo "<li".$selected."><a href=\"".get_page_link($page_id)."\">Contact</a></li>\n";
   				}
   				elseif ($page_name == "about_short") {/*ignore*/}
           	 	else {
            		(is_page($page_id))?$selected = ' class="selected"':$selected='';
            		echo "<li".$selected."><a href=\"".get_page_link($page_id)."\">$page_title</a></li>\n";
            	}
    		}
    	}
		?>
		<li><a href="/account/signup">Join</a></li>
	</ul>

</div><!-- /navigation -->

<hr class="low" />
