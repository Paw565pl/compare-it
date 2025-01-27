import {
  CategoriesList,
  FilterBar,
  ProductList,
} from "@/feature/product-list-page/components/index";

const ProductListPage = () => {
  return (
    <div className="flex w-auto flex-col bg-background p-0 lg:p-4">
      <div className="flex flex-col lg:flex-row">
        <div className="mr-0 w-full lg:mr-4 lg:w-1/5">
          <CategoriesList />
          <FilterBar />
        </div>
        <ProductList />
      </div>
    </div>
  );
};

export default ProductListPage;
