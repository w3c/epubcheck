const fs = require('fs-extra');
const Cache = require("@11ty/eleventy-cache-assets");

// get the latest release from github
async function getLabels() {
    try {
        let labels = await Cache("https://api.github.com/repos/w3c/epubcheck/labels", {
            duration: "1m", 
            type: "json"
        });
        
        // merge GitHub Labels with local long descriptions
        const long_descriptions = JSON.parse(fs.readFileSync('src/_data/labels_long_descriptions.json'));
        
        labels.forEach(function(label) {
			if (long_descriptions.hasOwnProperty(label.id)) {
				label.long_description = long_descriptions[label.id];
			}
		});

        return labels;
    }
    catch (err) {
        console.log("Error fetching latest release", err);
        return [];
    }
}

module.exports = async () => {
    // FIXME improve label retrieval’s performance?
    // let labels = await getLabels();
    let labels = {};
    return labels;
};