import { cn } from "@/core/utils/cn";
import { ComponentProps } from "react";
import { useDebounce } from "use-debounce";

interface TextareaProps extends ComponentProps<"textarea"> {
  readonly maxWordCount?: number;
}

export const Textarea = ({
  maxWordCount,
  className,
  ...props
}: TextareaProps) => {
  const [wordCount, setWordCount] = useDebounce(0, 100);

  return (
    <div className="space-y-2">
      <textarea
        data-slot="textarea"
        className={cn(
          "border-input placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-primary/50 aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive flex field-sizing-content max-h-48 min-h-24 w-full resize-y border bg-transparent px-3 py-2 text-base shadow-xs transition-[color,box-shadow] outline-none focus-visible:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
          className,
        )}
        {...props}
        onChange={(e) => {
          props.onChange?.(e);
          if (maxWordCount) setWordCount(e.target.value.trim().length);
        }}
      />
      {maxWordCount && (
        <div className="w-full text-right text-sm text-gray-500">
          {wordCount} / {maxWordCount}
        </div>
      )}
    </div>
  );
};
