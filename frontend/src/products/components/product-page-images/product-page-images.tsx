import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";

interface ProductPageImagesProps {
  readonly name: string;
  readonly images: string[];
}

export const ProductPageImages = ({ name, images }: ProductPageImagesProps) => {
  return (
    <div className="mt-4 flex flex-wrap justify-center gap-1">
      {images.map((imageUrl, imageUrlIndex) => (
        <ImageWithFallback
          key={imageUrlIndex}
          src={imageUrl}
          alt={name}
          width={300}
          height={265}
        />
      ))}
    </div>
  );
};
