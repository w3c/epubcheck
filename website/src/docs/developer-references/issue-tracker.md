---
title: Issue Tracker
---


We've set up a label system that allows us to quickly identify the various issues according to type, severity and priority.

The various labels can be combined to describe an issue, for example `priority: low`, `type: maintenance`, `status: accepted`, means that the issue is to be fixed, but with low priority (when there will be time) and that it is related to software maintenance operations.

See the [labels and their descriptions](https://github.com/w3c/epubcheck/labels) on the issue tracker on GitHub.

<!-- In the following table we explain the organization of the labels.


| Label | Short description | Long description |
|-------|-------------------|------------------|
{% for label in githubLabels %}| <span id="label_{{ label.id }}" aria-hidden="true">◼</span> {{ label.name }} | {{ label.description }} | {{ label.long_description }} |
{% endfor %}
<style>
	{% for label in githubLabels %}
		#label_{{ label.id }} {
			color: #{{ label.color }};
		}
	{% endfor %}
</style> -->