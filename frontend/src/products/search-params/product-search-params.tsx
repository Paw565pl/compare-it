import {
  createLoader,
  parseAsFloat,
  parseAsInteger,
  parseAsString,
} from "nuqs/server";

export const productFiltersSearchParams = {
  name: parseAsString,
  category: parseAsString,
  minPrice: parseAsFloat,
  maxPrice: parseAsFloat,
  shop: parseAsString,
} as const;

export const loadProductFiltersSearchParams = createLoader(
  productFiltersSearchParams,
);

export const productPaginationSearchParams = {
  page: parseAsInteger.withDefault(1),
  size: parseAsInteger.withDefault(20),
  sort: parseAsString.withDefault("offersCount,desc").withOptions({
    clearOnDefault: false,
  }),
} as const;

export const loadProductPaginationSearchParams = createLoader(
  productPaginationSearchParams,
);
