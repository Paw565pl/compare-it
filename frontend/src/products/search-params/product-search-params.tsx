import { categoryQueryParser } from "@/products/search-params/category-query-parser";
import { shopsQueryParser } from "@/products/search-params/shop-query-parser";

import {
  createLoader,
  parseAsArrayOf,
  parseAsBoolean,
  parseAsFloat,
  parseAsInteger,
  parseAsString,
} from "nuqs/server";

export const productFiltersSearchParams = {
  name: parseAsString,
  category: categoryQueryParser,
  shops: parseAsArrayOf(shopsQueryParser),
  minPrice: parseAsFloat,
  maxPrice: parseAsFloat,
  isAvailable: parseAsBoolean,
} as const;

export const loadProductFiltersSearchParams = createLoader(
  productFiltersSearchParams,
);

export const productPaginationSearchParams = {
  page: parseAsInteger.withDefault(1),
  size: parseAsInteger.withDefault(20),
  sort: parseAsString,
} as const;

export const loadProductPaginationSearchParams = createLoader(
  productPaginationSearchParams,
);
