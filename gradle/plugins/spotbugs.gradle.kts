// SpotBugs will use default HTML report only
spotbugs {
	ignoreFailures = true
	showProgress = true
	showStackTraces = true
	reportsDir = layout.buildDirectory.dir("reports/spotbugs")
}
