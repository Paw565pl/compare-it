import { ProductPageTop } from "@/products/components/index";

export interface ProductPageProps {
  readonly params: Promise<{ id: string }>;
}

export const ProductPage = async ({ params }: ProductPageProps) => {
  const productId = (await params).id;

  return (
    <div className="flex flex-col">
      <ProductPageTop productId={productId} />
    </div>
  );
};
