# Run Kokoroid

[English](README.md)/[简体中文](./README_zhCN.md)

Run-Kokoroid is a Gradle plugin for debugging your Kokoroid extensions within Gradle.

Inspired by: https://github.com/jpenilla/run-task

## Usage

1. Apply the run-kokoroid plugin.
2. Configure the run-kokoroid extension.
```kotlin
// You basically only need to configure these items.
runKokoroid {
   testExtensionType = "driver" // Required: The type of test extension. It can be "driver", "plugin", or "adapter".

   githubToken = System.getenv("GITHUB_TOKEN") // Your GitHub Token. This is useful if you are limited by API rate limits.
   isValidationOnly = true // Whether to only perform the validation process. This corresponds to Kokoroid's `--validationOnly` option.
   enableKokoroidDebug = true // Whether to enable Kokoroid debug mode. This corresponds to Kokoroid's `--debug` option.
}
```

3. Use the `runKokoroid` task to start Kokoroid.

## License

run-kokoroid is licensed under the LGPL-2.1 License. For more information, please visit [Wikipedia - LGPL V2.1](https://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License#Version_2.1).