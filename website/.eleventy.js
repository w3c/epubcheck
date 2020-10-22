const path = require('path');
const slug = require('slugify')
const globby = require ('globby').sync

// Customize slugify
const slugify = s => slug(s,{lower: true})

// Eleventy Plugins
const navigationPlugin = require("@11ty/eleventy-navigation");
const syntaxHighlightPlugin = require("@11ty/eleventy-plugin-syntaxhighlight");

// Markdown-it & Plugins
const markdownIt = require("markdown-it");
const markdownItAnchor = require("markdown-it-anchor");
const markdownItContainer = require("markdown-it-container");
const markdownItDeflist = require("markdown-it-deflist");
const markdownItReplaceLink = require("markdown-it-replace-link");
const markdownItToc = require("./src/_11ty/markdown/markdown-it-toc-to-env");

// Local Data
const site = require("./src/_data/site");

// Get the production build flag
const isProduction = process.env.NODE_ENV === 'production';

// Helper function to apply a configuration function to a glob of JS file
// - If the JS file exports a function, the configuration function is applied
//   with that function as argument, using the file name as the registration
//   name
// - If the JS file exports an object of functions, the configuration function
//   is applied for each entry, using the entry name as the registration name
const applyConfigFunction = function(configurationFunction,glob) {
	globby(glob)
	.map(jsPath => {
		return {
			name: path.basename(jsPath,'.js'),
			id: `./${jsPath.replace(/\.js$/,'')}`
		}
	})
	.forEach(js => {
			var objectOrFunc = require(js.id)
				if (objectOrFunc.constructor === Function) {
					configurationFunction.call(this,js.name,objectOrFunc)
				} else {
					Object.entries(objectOrFunc).forEach(([name,entry]) => {
						configurationFunction.call(this,name,entry)
					});	
				}

	})
}

module.exports = config => {
	const apply = applyConfigFunction.bind(config)

	//
	//		PLUGINS
	//___________________________________________________________________________

	config.addPlugin(navigationPlugin);
	config.addPlugin(syntaxHighlightPlugin);

	//
	//		FILTERS
	//___________________________________________________________________________

	apply(config.addFilter,'src/_11ty/filters/*.js')
	
	//
	//		SHORTCODES
	//___________________________________________________________________________
	apply(config.addShortcode,'src/_11ty/shortcodes/*.js')
	apply(config.addPairedShortcode,'src/_11ty/shortcodes-paired/*.js')
	apply(config.addNunjucksAsyncShortcode,'src/_11ty/shortcodes-async/*.js')

	//
	//		MARKDOWN
	//___________________________________________________________________________

	// See also 
	// - https://github.com/arve0/markdown-it-attrs to add classes and attributes
	// - https://github.com/mb21/markdown-it-bracketed-spans for span markup
	
	config.setLibrary("md", markdownIt({
		html: true,
		breaks: false,
		linkify: true,
		typographer: true,
		// Automatically apply the URL filter at the markdown-render level
		replaceLink: (link,env) => (link.startsWith('/'))?config.getFilter('url')(link):link
	})
	.use(markdownItDeflist)
	.use(markdownItReplaceLink)
	.use(markdownItContainer,'note', {
		render: function (tokens, idx) {
			// extract the note class
			var type = tokens[idx].info.trim().match(/^note\s+(.+)$/);
			if (tokens[idx].nesting === 1) {
				// opening tag
				return `<aside role="note"${(type)?`class="${type[1]}"`:''}>`;
				// return `<aside role="note"${(type)?`class="${type[1]}"`:''}>`;
			} else {
				// closing tag
				return '</aside>\n';
			}
		}
	})
	.use(markdownItContainer,'quicktip')
	// Add slugified IDs to headings
	.use(markdownItAnchor, {
		permalink: true,
		permalinkClass: "direct-link",
		permalinkSymbol: "§",
		permalinkBefore: true,
		slugify,
	})
	// Augment the template data with a JSON ToC
	// Can be replaced by 'markdown-it-toc-done-right' after these issues are fixed:
	// - https://github.com/nagaozen/markdown-it-toc-done-right/issues/46
	// - https://github.com/nagaozen/markdown-it-toc-done-right/issues/47
	.use(markdownItToc, {
		slugify
	})
	);


	//
	//		BROWSERSYNC
	//___________________________________________________________________________

	config.setBrowserSyncConfig({
		// Watch externally-built CSS and JS files
		files: ['_site/css/*.css','_site/js/*.js'],
		callbacks: {
			ready: (err, bs) => {
				// Register a middleware at the end of the stack
				// to redirect to the 404 page (as a convenience
				// only, for development mode).
				bs.addMiddleware("*", function (req, res) {
						res.writeHead(302, {
								location: config.getFilter("url")("/404.html")
						});
						res.end("Redirecting!");
				});
			}
		}
	});
	

	
	//
	//		PASSTHROUGH
	//___________________________________________________________________________

	config.addPassthroughCopy("src/robots.txt");
	config.addPassthroughCopy("src/favicon.ico");
	config.addPassthroughCopy("src/fonts/");

	//
	//		OTHER ELEVENTY SETTINGS
	//___________________________________________________________________________

	// Define own ignores in .eleventyignore (instead of .gitignore)
	config.setUseGitIgnore(false);
	// Opt for a full deep merge /w the data cascade
	config.setDataDeepMerge(true);
	// Reduce output verbosity
	config.setQuietMode(true);


	return {
		templateFormats: ["html", "md", "njk"],
		markdownTemplateEngine: 'njk',
		dataTemplateEngine: 'njk',
		htmlTemplateEngine: 'njk',
		passthroughFileCopy: true,
		pathPrefix: site.pathPrefix,
		dir: {
			input: "src",
			output: `_site/${process.env.DOCSSITE_WRITE_VERSION==='true' ? 
				`version/${process.env.DOCSSITE_VERSION}` : ''}`
		}
	};
};
