import { ProductImage } from "@/products/components/index";
interface ProductPageImagesProps {
  readonly name: string;
  readonly images: string[];
}

export const ProductPageImages = ({ name, images }: ProductPageImagesProps) => {
  return (
    <div className="mt-4 flex flex-wrap gap-1">
      {images.map((imageUrl) => (
        <ProductImage name={name} imageUrl={imageUrl} />
      ))}
    </div>
  );
};
