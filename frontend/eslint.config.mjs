// @ts-check

import { includeIgnoreFile } from "@eslint/compat";
import { FlatCompat } from "@eslint/eslintrc";
import eslint from "@eslint/js";
import tanstackQueryPlugin from "@tanstack/eslint-plugin-query";
import eslintConfigPrettier from "eslint-config-prettier/flat";
import noRelativeImportPaths from "eslint-plugin-no-relative-import-paths";
import { defineConfig } from "eslint/config";
import { dirname } from "path";
import tseslint from "typescript-eslint";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const gitIgnorePath = fileURLToPath(new URL(".gitignore", import.meta.url));

const compat = new FlatCompat({
  baseDirectory: __dirname,
});

const eslintConfig = defineConfig(
  includeIgnoreFile(gitIgnorePath),
  eslint.configs.recommended,
  tseslint.configs.strict,
  tseslint.configs.stylistic,
  tanstackQueryPlugin.configs["flat/recommended"],
  ...compat.extends("next/core-web-vitals", "plugin:jsx-a11y/strict"),
  {
    plugins: {
      "no-relative-import-paths": noRelativeImportPaths,
    },
    rules: {
      "no-relative-import-paths/no-relative-import-paths": [
        "error",
        { rootDir: "src", prefix: "@", allowSameFolder: false },
      ],
    },
  },
  eslintConfigPrettier,
);

export default eslintConfig;
