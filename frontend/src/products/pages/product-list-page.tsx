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

export const ProductListPage = async ({
  searchParams,
}: PageProps<"/produkty">) => {
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
        <aside className="mr-0 hidden w-full lg:mr-4 lg:block lg:w-1/5">
          <CategoriesList />
          <FiltersBar />
        </aside>

        <section className="flex w-full flex-col">
          <ProductListPageHeader />
          <ProductList />
        </section>
      </section>
    </HydrationBoundary>
  );
};
