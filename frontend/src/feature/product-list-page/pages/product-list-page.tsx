import {
  SearchBar,
  SingleProduct,
} from "@/feature/product-list-page/components";

const ProductListPage = () => {
  return (
    <div className="flex flex-col items-center justify-center bg-slate-300">
      <SearchBar />
      {/* <ProductList /> */}
      <SingleProduct />
    </div>
  );
};

export default ProductListPage;
