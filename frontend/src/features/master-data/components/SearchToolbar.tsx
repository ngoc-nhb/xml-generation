import { Search } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

interface SearchToolbarProps {
    value: string;
    placeholder?: string;
    onChange: (value: string) => void;
    onSearch: () => void;
}

export function SearchToolbar({ value, placeholder = 'Search…', onChange, onSearch }: SearchToolbarProps) {
    return (
        <div className="flex flex-col gap-3 sm:flex-row">
            <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    className="pl-9"
                    placeholder={placeholder}
                    value={value}
                    onChange={(event) => onChange(event.target.value)}
                    onKeyDown={(event) => {
                        if (event.key === 'Enter') {
                            onSearch();
                        }
                    }}
                />
            </div>
            <Button variant="secondary" onClick={onSearch}>
                Search
            </Button>
        </div>
    );
}
