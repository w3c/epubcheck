const moment = require('moment');
const path = require('path');
const Cache = require("@11ty/eleventy-cache-assets");

// get the latest release from github
async function getLatestRelease() {
    try {
        let release = await Cache("https://api.github.com/repos/w3c/epubcheck/releases/latest", {
            duration: "1m", 
            type: "json"
        });

        return {
            url: release.assets[0].browser_download_url,
            version: release.tag_name,
            date: moment(release.published_at).format('YYYY-MM-DD'),
            name: release.assets[0].name,
            filename: path.basename(release.assets[0].name, '.zip')
        };
    }
    catch (err) {
        console.log("Error fetching latest release", err);
        return {
            url: "https://github.com/w3c/epubcheck/releases/",
            version: "",
            date: ""
        };
    }
}

module.exports = async () => {
    let latest = await getLatestRelease();
    return {latest};
};