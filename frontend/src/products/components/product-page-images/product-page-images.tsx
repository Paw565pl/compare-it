import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";

interface ProductPageImagesProps {
  readonly name: string;
  readonly images: string[];
}

export const ProductPageImages = ({ name, images }: ProductPageImagesProps) => {
  return (
    <div className="mt-4 flex flex-wrap justify-center gap-3">
      {images.map((imageUrl, imageUrlIndex) => (
        <ImageWithFallback
          key={imageUrlIndex}
          src={imageUrl}
          alt={name}
          fill
          sizes="(max-width: 359px) 100vw, 320px"
          containerClassName="w-75 h-66.25 bg-white"
        />
      ))}
    </div>
  );
};
