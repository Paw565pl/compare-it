import {
  CategoriesList,
  FilterBar,
  ProductList,
  SearchBar,
} from "@/feature/product-list-page/components/index";

const ProductListPage = () => {
  return (
    <div className="flex flex-col justify-center bg-slate-300 p-8">
      <SearchBar />
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
