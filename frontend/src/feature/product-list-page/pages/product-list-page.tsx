import {
  CategoriesList,
  FilterBar,
  ProductList,
} from "@/feature/product-list-page/components/index";

const ProductListPage = () => {
  return (
    <div className="flex w-auto flex-col bg-gray-100 p-4">
      <div className="flex">
        <div className="mr-4 w-1/5">
          <CategoriesList />
          <FilterBar />
        </div>
        <ProductList />
      </div>
      {/* <SingleProduct /> */}
    </div>
  );
};

export default ProductListPage;
