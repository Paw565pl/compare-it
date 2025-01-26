import {
  CategoriesList,
  FilterBar,
  ProductList,
} from "@/feature/product-list-page/components/index";

const ProductListPage = () => {
  return (
    <div className="flex w-auto flex-col bg-background p-0 sm:p-4">
      <div className="flex flex-col sm:flex-row">
        <div className="mr-0 w-full sm:mr-4 sm:w-1/5">
          <CategoriesList />
          <FilterBar />
        </div>
        <ProductList />
      </div>
    </div>
  );
};

export default ProductListPage;
