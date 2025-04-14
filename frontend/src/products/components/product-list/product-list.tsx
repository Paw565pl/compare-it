"use client";

import { H2 } from "@/core/components/ui/h2";
import {
  ProductPagination,
  SingleProduct,
  SortSelect,
} from "@/products/components";
import { useFetchProductPage } from "@/products/hooks/client/use-fetch-product-page";
import {
  productFiltersSearchParams,
  productPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";

export const ProductList = () => {
  const [filters] = useQueryStates(productFiltersSearchParams);
  const [pagination] = useQueryStates(productPaginationSearchParams, {
    scroll: true,
  });

  const {
    data: productsPage,
    isLoading,
    error,
  } = useFetchProductPage(filters, {
    ...pagination,
    page: Math.max(0, pagination.page - 1),
  });

  if (isLoading) return <div className="text-primary">Ładowanie...</div>;
  if (error || !productsPage)
    return <div className="text-red-600">Coś poszło nie tak!</div>;

  const hasNextPage =
    productsPage.page.number + 1 < productsPage.page.totalPages;

  return (
    <>
      <div className="mb-1 flex justify-between">
        <H2>Produkty</H2>
        <SortSelect />
      </div>

      <ul className="space-y-2">
        {productsPage.content.map((product) => (
          <li key={product.id}>
            <SingleProduct product={product} />
          </li>
        ))}
      </ul>

      <ProductPagination hasNextPage={hasNextPage} />
    </>
  );
};
