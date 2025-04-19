import { H1 } from "@/core/components/ui/h1";
import { SortSelect } from "@/products/components";

export const ProductListPageHeader = () => {
  return (
    <header className="mt-5 mb-1 flex justify-between lg:mt-0">
      <H1 className="mb-0">Produkty</H1>
      <SortSelect />
    </header>
  );
};
