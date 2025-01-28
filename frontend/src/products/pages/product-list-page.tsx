import {
  CategoriesList,
  FilterBar,
  ProductList,
} from "@/products/components/index";
import { Suspense } from "react";

const ProductListPage = () => {
  return (
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
  );
};

export default ProductListPage;
