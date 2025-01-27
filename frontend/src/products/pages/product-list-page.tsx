import { getQueryClient } from "@/core/libs/tanstack-query";
import {
  CategoriesList,
  FilterBar,
  ProductList,
} from "@/products/components/index";
import { prefetchCategoriesList } from "@/products/hooks/server/prefetch-categories-list";
import { prefetchProductPage } from "@/products/hooks/server/prefetch-product-page";
import { prefetchShopsList } from "@/products/hooks/server/prefetch-shops-list";
import { dehydrate, HydrationBoundary } from "@tanstack/react-query";
import { Suspense } from "react";

const ProductListPage = () => {
  const queryClient = getQueryClient();
  prefetchCategoriesList(queryClient);
  prefetchProductPage(queryClient);
  prefetchShopsList(queryClient);

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <div className="flex w-auto flex-col bg-background p-0 lg:p-4">
        <div className="flex flex-col lg:flex-row">
          <div className="mr-0 w-full lg:mr-4 lg:w-1/5">
            <Suspense>
              <CategoriesList />
            </Suspense>
            <Suspense>
              <FilterBar />
            </Suspense>
          </div>
          <div className="flex w-full flex-col">
            <Suspense>
              <ProductList />
            </Suspense>
          </div>
        </div>
      </div>
    </HydrationBoundary>
  );
};

export default ProductListPage;
