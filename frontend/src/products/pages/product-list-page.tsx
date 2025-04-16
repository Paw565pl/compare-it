import { getQueryClient } from "@/core/libs/tanstack-query";
import {
  CategoriesList,
  FiltersBar,
  ProductList,
  ProductListPageHeader,
} from "@/products/components";
import { prefetchCategoriesList } from "@/products/hooks/server/prefetch-categories-list";
import { prefetchProductPage } from "@/products/hooks/server/prefetch-product-page";
import { prefetchShopsList } from "@/products/hooks/server/prefetch-shops-list";
import {
  loadProductFiltersSearchParams,
  loadProductPaginationSearchParams,
} from "@/products/search-params/product-search-params";
import { dehydrate, HydrationBoundary } from "@tanstack/react-query";
import { SearchParams } from "nuqs";
import { Suspense } from "react";

interface ProductListPageProps {
  readonly searchParams: Promise<SearchParams>;
}

export const ProductListPage = async ({
  searchParams,
}: ProductListPageProps) => {
  const queryClient = getQueryClient();

  const productFilters = await loadProductFiltersSearchParams(searchParams);
  const productPagination =
    await loadProductPaginationSearchParams(searchParams);

  await Promise.all([
    prefetchProductPage(queryClient, productFilters, {
      ...productPagination,
      page: productPagination.page - 1,
    }),
    prefetchCategoriesList(queryClient),
    prefetchShopsList(queryClient),
  ]);

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <section className="flex flex-col lg:flex-row xl:p-4">
        <aside className="mr-0 w-full lg:mr-4 lg:w-1/5">
          <Suspense>
            <CategoriesList />
          </Suspense>

          <Suspense>
            <FiltersBar />
          </Suspense>
        </aside>

        <section className="flex w-full flex-col">
          <Suspense>
            <ProductListPageHeader />
          </Suspense>

          <Suspense>
            <ProductList />
          </Suspense>
        </section>
      </section>
    </HydrationBoundary>
  );
};
