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
var PAGE_SIZE = 100;
var METRIC_KEYS = 'num_vuln_low, num_vuln_med, num_vuln_high, hub_component_names, num_components_rating';
var MAX_COMPONENTS_PER_ROW = 5;

window.registerExtension('hubsonarqube/overview', function (options) {
	window.globalOptions = options;
	window.isDisplayed = true;
	window.riskSorted = true;
	window.ratingSorted = false;
	window.componentsArray = [];
	
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
    
    var link_span = document.createElement('span');
    link_span.setAttribute('id', 'blackduck_link_span');
    link_span.innerHTML = 'Click on the vulnerable component versions for remediation information.';
    wrapper.appendChild(link_span);

    var loadingGif = document.createElement('img');
    loadingGif.setAttribute('id', 'blackduck_loading');
    loadingGif.setAttribute('src', '/static/hubsonarqube/loading.gif');
    loadingGif.setAttribute('alt', 'Loading...');
    wrapper.appendChild(loadingGif);
    
    options.el.appendChild(wrapper);
    
    getAndDisplayData(wrapper);
	return function () {
		window.isDisplayed = false;
	};
});

function getAndDisplayData(wrapper, page = 1) {
	window.SonarRequest.getJSON('/api/measures/component_tree', {
		baseComponentKey: window.globalOptions.component.key,
		p: page,
		ps: PAGE_SIZE,
		metricKeys: METRIC_KEYS
	}).then(function (response) {
		window.componentsArray = window.componentsArray.concat(response.components);
		if (page > (response.paging.total / PAGE_SIZE)) {
			handleResponse(wrapper);
			var loadingGif = document.getElementById('blackduck_loading');
			loadingGif.remove();
		} else {
			getAndDisplayData(wrapper, page + 1);
		}
	});
}

function handleResponse(wrapper) {
	if (window.componentsArray != null && window.componentsArray.length != 0) {
		displayMainTable(wrapper, window.componentsArray, window.isDisplayed);
	} else {
		var message = document.createElement('p');
		message.innerHTML = 'No Hub component data to display...';
		wrapper.append(message);
		window.globalOptions.el.appendChild(wrapper);
	}
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
					formatTableHead('File') +
					formatTableHead('Security Risk', 'sortOnRisk', true) +
					formatTableHead('Vulnerable Components') +
					formatTableHead('Rating', 'sortOnRating', true) +
				'</tr>' +
				tableRowsAsString +
			'</tbody>';
		parentElement.appendChild(table);
		
		window.globalOptions.el.appendChild(parentElement);
	}
}

function formatTableHead(value, fnName = '', center = false) {
	var beginTag = '<th><strong>';
	var endTag = '</strong></th>';
	if (center) {
		beginTag = '<th style="text-align:center;"><strong>';
	}
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
			helper.comps = parseComponents(curValue);
		} else if (curMetric == 'num_components_rating') {
			helper.rating = curValue;
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
			tableRows += '<tr><td><i class="icon-qualifier-fil"></i> '
				+ curComp.name + '</td><td style="text-align:center;"><span title="High" id="highRisk">'
				+ curComp.high + '</span> <span title="Medium" id="medRisk">'
				+ curComp.med + '</span> <span title="Low" id="lowRisk">'
				+ curComp.low + '</span></td><td>'
				+ curComp.comps + '</td><td style="text-align:center;">'
				+ getRating(curComp.rating)
				+ '</td></tr>';
		} else {
			continue;
		}
	}
	return tableRows;
}

function getRating(givenRating) {
	var intRating = parseInt(givenRating);
	var charRating = 'E';
	switch (intRating) {
		case 1:
			charRating = 'A';
			break;
		case 2:
			charRating = 'B';
			break;
		case 3:
			charRating = 'C';
			break;
		case 4:
			charRating = 'D';
			break;
		case 5:
			charRating = 'E';
			break;
		case 6:
			charRating = 'F';
			break;
	}
	return '<span title="' + getToolTip(charRating) + '" class="rating rating-'+ charRating + '">' + charRating + '</span>';
}

function getToolTip(rating) {
	var prefix = 'At least one ';
	var suffix = ' security vulnerability is present.';
	var fix;
	switch (rating) {
		case 'A':
			return 'No security vulnerabilities exist!';
		case 'B':
			fix = 'low';
			break;
		case 'C':
			fix = 'medium';
			break;
		case 'D':
			fix = 'high';
			break;
		default:
			return '';
	}
	return prefix + fix + suffix;
}

function parseComponents(componentCsv) {
	var componentArray = componentCsv.split(',');
	var components = '';
	var flag = false;
	var lastIndex = componentArray.length - 3;
	for (var i = 0; i < lastIndex; i += 3) {
		if (i == (MAX_COMPONENTS_PER_ROW * 3) - 1) {
			components += '<a onclick="seeMoreComponents(this);">See more...<br /></a>';
			components += '<div class="expandableTableRow">';
			flag = true;
		}
		components += componentArray[i] + formatComponentVersion(componentArray[i + 1], componentArray[i + 2]) + '<br />';
	}
	if (lastIndex >= 0) {
		components += componentArray[lastIndex] + formatComponentVersion(componentArray[lastIndex + 1], componentArray[lastIndex + 2]);
		if (flag) {
			components += '</div>';
		}
	}
	return components;
}

function formatComponentVersion(version, link) {
	return ' <span id="blackduck_version"><a title="Click here for remediation information." target="_blank" href="' + link + '">' + version + '</a></span>';
}

function seeMoreComponents(element) {
	var sibling = element.parentElement.getElementsByTagName('div')[0];
	sibling.style['max-height'] = "100%";
	sibling.style.overflow = "visible";
	element.remove();
}

function sortOnRisk() {
	sortColumn(1, sortInt, window.riskSorted);
	window.riskSorted = !window.riskSorted;
	window.ratingSorted = false;
}

function sortInt(tdX, tdY, reverse, index = 0) {
	if (index < 3) {
		var x = tdX.getElementsByTagName('span')[index];
		var y = tdY.getElementsByTagName('span')[index];
		var xContent = parseInt(x.innerHTML);
		var yContent = parseInt(y.innerHTML);
		if (xContent == yContent) {
			return sortInt(tdX, tdY, reverse, index + 1);
		} else {
			return reverse ? xContent > yContent : xContent < yContent;
		}
	}
	return false;
}

function sortOnRating() {
	sortColumn(3, sortChar, !window.ratingSorted);
	window.ratingSorted = !window.ratingSorted;
	window.riskSorted = false;
}

function sortChar(tdX, tdY, reverse) {
	var xContent = tdX.innerHTML;
	var yContent = tdY.innerHTML;
	return reverse ? xContent < yContent : xContent > yContent;
}

function sortColumn(index, sortFunc, reverse) {
		var table = document.getElementById('blackduck_table');
		var rows = table.getElementsByTagName('tr');
		var switching = true;
		var i;
		while (switching) {
			switching = false;
			var shouldSwitch = false;
			for (i = 1; i < (rows.length - 1); i++) {
				var x = rows[i].getElementsByTagName('td')[index];
				var y = rows[i + 1].getElementsByTagName('td')[index];
				shouldSwitch = sortFunc(x, y, reverse);
				if (shouldSwitch) {
					break;
				}
			}
			if (shouldSwitch) {
				rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
				switching = true;
			}
		}
}

function resetTable() {
	var wrapper = document.getElementById('blackduck_wrapper');
	var table = wrapper.getElementsByTagName('table')[0];
	table.remove();
	displayMainTable(wrapper, window.componentsArray, window.isDisplayed);
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