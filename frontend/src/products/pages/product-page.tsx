import { ProductPageTop } from "@/products/components/index";

export interface ProductPageProps {
  readonly id: string;
}

export const ProductPage = ({ id }: ProductPageProps) => {
  return (
    <div className="flex flex-col">
      <ProductPageTop id={id as string} />
    </div>
  );
};
