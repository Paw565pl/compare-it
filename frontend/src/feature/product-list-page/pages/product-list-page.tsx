import {
  SearchBar,
  SingleProduct,
  ProductList,
  FilterBar,
  CategoriesList
} from "@/feature/product-list-page/components/index";

const ProductListPage = () => {
  return (
    <div className="flex flex-col justify-center bg-slate-300 p-8">
      <SearchBar />
      {/* <ProductList /> */}
      {/* <FilterBar /> */}
      <CategoriesList />
      <FilterBar />
      {/* <SingleProduct /> */}
    </div>
  );
};

export default ProductListPage;
