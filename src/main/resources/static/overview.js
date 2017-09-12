/*
 * Black Duck Hub Plugin for SonarQube
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var PLUGIN_NAME = 'Black Duck Hub Plugin for SonarQube';
var MAX_COMPONENTS_PER_ROW = 5;

window.registerExtension('hubsonarqube/overview', function (options) {
	window.globalOptions = options;
	window.isDisplayed = true;
	window.tableSorted = true;
	
	var wrapper = document.createElement('div');
	wrapper.setAttribute('id', 'blackduck_wrapper');
	
	var head = document.getElementsByTagName('head')[0];
	var stylesheet = document.createElement('link');
	stylesheet.setAttribute('href', '/static/hubsonarqube/blackduck.css');
	stylesheet.setAttribute('rel', 'stylesheet');
	head.appendChild(stylesheet);

	var header = document.createElement('span');
	header.setAttribute('class', 'large_header');
    header.textContent = PLUGIN_NAME;
    wrapper.appendChild(header);

    // TODO find a way to get the base component key
	window.SonarRequest.getJSON('/api/measures/component_tree', {
		baseComponentKey: 'blackduck:hub',
		ps: 500,
		metricKeys: 'num_vuln_low, num_vuln_med, num_vuln_high, hub_component_names'
	}).then(function (response) {
		window.componentsArray = response.components;
		displayMainTable(wrapper, window.componentsArray, window.isDisplayed);
	});
	return function () {
		window.isDisplayed = false;
	};
});

function resetTable() {
	var wrapper = document.getElementById('blackduck_wrapper');
	var table = wrapper.getElementsByTagName('table')[0];
	table.innerHTML = '';
	table.remove();
	displayMainTable(wrapper, window.componentsArray, window.isDisplayed);
}

function displayMainTable(parentElement, componentsArray, visible) {
	if (visible) {
		var componentHelperArray = getComponentHelperObjects(componentsArray);
		var tableRowsAsString = getTableRowsAsString(componentHelperArray);
		
		var table = document.createElement('table');
		table.setAttribute('id', 'blackduck_table');
		table.innerHTML =
			'<tbody>' +
				'<tr>' + 
					formatTableHead('File', '') +
					formatTableHead('Low', 'sortOnLow') +
					formatTableHead('Med', 'sortOnMed') +
					formatTableHead('High', 'sortOnHigh') +
					formatTableHead('Vulnerable Components', '') +
				'</tr>' +
				tableRowsAsString +
			'</tbody>';
		parentElement.appendChild(table);
		
		window.globalOptions.el.appendChild(parentElement);
		getSetting('sonar.hub.url', linkComponentsToHub);
	}
}

function formatTableHead(value, fnName) {
	var beginTag = '<th><strong>';
	var endTag = '</strong></th>';
	if (fnName != '') {
		return beginTag + '<a onclick="' + fnName + '();">' + value + '</a>' + endTag;
	}
	return beginTag + value + endTag;
}

function getComponentHelperObjects(componentsArray) {
	var vulnerableComponents = [];
	var i = 0;
	for(i; i < componentsArray.length; i++) {
		var curComponent = componentsArray[i];
		if (curComponent.qualifier == 'FIL') {
			var filePath = curComponent.path;
			var measuresArray = curComponent.measures;
			vulnerableComponents[vulnerableComponents.length] = getComponentHelperObject(filePath, measuresArray);
		}
	}
	return vulnerableComponents;
}

function getComponentHelperObject(fileName, measuresArray) {
	var helper = new Object();
	helper.name = fileName;
	helper.low = 0;
	helper.med = 0;
	helper.high = 0;
	
	for (var i = 0; i < measuresArray.length; i++) {
		var curMetric = measuresArray[i].metric;
		var curValue = measuresArray[i].value;
		if (curMetric == 'num_vuln_low') {
			helper.low = curValue;
		} else if (curMetric == 'num_vuln_med') {
			helper.med = curValue;
		} else if (curMetric == 'num_vuln_high') {
			helper.high = curValue;
		} else if (curMetric == 'hub_component_names') {
			helper.comps = parseComponents(curValue); // getComponentsAsArray(curValue);
		}
	}
	return helper;
}

function getTableRowsAsString(componentHelperArray) {
	var high = [];
	var med = [];
	var low = [];
	
	for (var i = 0; i < componentHelperArray.length; i++) {
		var curComp = componentHelperArray[i];
		if (curComp.high > 0) {
			high[high.length] = curComp;
		} else if (curComp.med > 0) {
			med[med.length] = curComp;
		} else {
			low[low.length] = curComp;
		}
	}
	
	high.sort(compareHigh);
	med.sort(compareMed);
	low.sort(compareLow);
	
	var sortedComponents = high.concat(med, low);
	var tableRows = '';
	for (var i = 0; i < sortedComponents.length; i++) {
		var curComp = sortedComponents[i];
		if ((curComp.low + curComp.med + curComp.high) > 0 && curComp.comps != '') {
			var textLeft = 'style="text-align:left;"';
			tableRows += '<tr><td ' + textLeft + '>' 
				+ curComp.name + '</td><td>' 
				+ curComp.low + '</td><td>' 
				+ curComp.med + '</td><td>' 
				+ curComp.high + '</td><td ' + textLeft + '>' 
				+ curComp.comps 
				+ '</td></tr>';
		} else {
			continue;
		}
	}
	return tableRows;
}

function parseComponents(componentCsv) {
	var componentArray = componentCsv.split(',');
	var components = '';
	
	var lastIndex = componentArray.length - 1;
	for (var i = 0; i < lastIndex; i++) {
		if (i >= MAX_COMPONENTS_PER_ROW) {
			components += '<a class="expandableTableRow">See more...</a>';
			return components;
		}
		components += componentArray[i] + '<br />';
	}
	if (lastIndex >= 0) {
		components += componentArray[lastIndex];
	}
	return components;
}

function getSetting(settingsKey, callback) {
	window.SonarRequest.getJSON('/api/settings/values', {
	}).then(function (response) {
		var globalSettings = response.settings;
		for (var i = 0; i < globalSettings.length; i++) {
			var curSetting = globalSettings[i];
			if (curSetting.key == settingsKey) {
				callback(curSetting.value);
				break;
			}
		}
	});
}

function linkComponentsToHub(link) {
	var componentLinks = document.getElementsByClassName('expandableTableRow');
	for (var i = 0; i < componentLinks.length; i++) {
		componentLinks[i].setAttribute('href', link);
	}
}

function sortOnFileName() {
	sortIntColumn(0);
}

function sortOnLow() {
	sortIntColumn(1);
}

function sortOnMed() {
	sortIntColumn(2);
}

function sortOnHigh() {
	sortIntColumn(3);
}

function sortIntColumn(index) {
	if (window.tableSorted) {
		var table = document.getElementById('blackduck_table');
		var rows = table.getElementsByTagName('tr');
		var switching = true;
		var i;
		while (switching) {
			switching = false;
			var shouldSwitch;
			for (i = 1; i < (rows.length - 1); i++) {
				shouldSwitch = false;
				var x = rows[i].getElementsByTagName('td')[index];
				var y = rows[i + 1].getElementsByTagName('td')[index];
				var xContent = parseInt(x.innerHTML);
				var yContent = parseInt(y.innerHTML);
				if (xContent < yContent) {
					shouldSwitch = true;
					break;
				}
			}
			if (shouldSwitch) {
				rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
				switching = true;
			}
		}
		window.tableSorted = false;
	} else {
		resetTable();
		window.tableSorted = true;
	}
}

function compareHigh(a,b) {
	if (a.high > b.high)
		return -1;
	if (a.high < b.high)
		return 1;
	return compareMed(a,b);
}

function compareMed(a,b) {
	if (a.med > b.med)
		return -1;
	if (a.med < b.med)
		return 1;
	return compareLow(a,b);
}

function compareLow(a,b) {
	if (a.low > b.low)
		return -1;
	if (a.low < b.low)
		return 1;
	return 0;
}